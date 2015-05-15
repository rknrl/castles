//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.database

import akka.actor.Actor
import com.github.mauricio.async.db.mysql.pool.MySQLConnectionFactory
import com.github.mauricio.async.db.pool.{ConnectionPool, PoolConfiguration}
import com.github.mauricio.async.db.util.ExecutorServiceUtils.CachedExecutionContext
import com.github.mauricio.async.db.{Configuration, ResultSet, RowData}
import ru.rknrl.castles.database.Database._
import ru.rknrl.castles.matchmaking.{Top, TopUser}
import ru.rknrl.dto._
import ru.rknrl.logging.ActorLog

class DbConfiguration(username: String,
                      host: String,
                      port: Int,
                      password: String,
                      database: String,
                      poolMaxObjects: Int,
                      poolMaxIdle: Long,
                      poolMaxQueueSize: Int) {
  def configuration = Configuration(
    username = username,
    host = host,
    port = port,
    password = Some(password),
    database = Some(database)
  )

  def poolConfiguration = PoolConfiguration(
    maxObjects = poolMaxObjects,
    maxIdle = poolMaxIdle,
    maxQueueSize = poolMaxQueueSize
  )
}

object Database {

  trait Request {
    val accountId: AccountId
  }

  trait Response {
    val accountId: AccountId
  }

  trait NoResponse

  /** Ответом будет List[TopItem] */
  case object GetTop

  case class GetAccount(accountId: AccountId) extends Request

  case class AccountResponse(accountId: AccountId,
                             state: Option[AccountStateDTO],
                             rating: Option[Double],
                             tutorState: Option[TutorStateDTO],
                             place: Long) extends Response

  case class GetAndUpdateAccountState(accountId: AccountId, transform: Option[AccountStateDTO] ⇒ AccountStateDTO) extends Request

  case class AccountStateResponse(accountId: AccountId, state: AccountStateDTO) extends Response

  case class GetAndUpdateAccountStateAndRating(accountId: AccountId, transform: (Option[AccountStateDTO], Option[Double]) ⇒ (AccountStateDTO, Double)) extends Request

  case class AccountStateAndRatingResponse(accountId: AccountId, state: AccountStateDTO, rating: Double, place: Long) extends Response

  case class UpdateTutorState(accountId: AccountId, tutorState: TutorStateDTO) extends NoResponse

  case class UpdateUserInfo(accountId: AccountId, userInfo: UserInfoDTO) extends NoResponse

}

class Database(configuration: DbConfiguration) extends Actor with ActorLog {

  val factory = new MySQLConnectionFactory(configuration.configuration)
  val pool = new ConnectionPool(factory, configuration.poolConfiguration)

  override def receive = logged {
    case GetTop ⇒
      val ref = sender
      read(
        "SELECT user_info.id,coalesce(ratings.rating,0) rating,userInfo " +
          "FROM user_info " +
          "LEFT JOIN ratings ON ratings.id=user_info.id " +
          "GROUP BY ratings.id " +
          "ORDER BY coalesce(ratings.rating,0) DESC " +
          "LIMIT 5;",
        Seq.empty,
        resultSet ⇒ send(ref, Top(resultSet.map(rowDataToTopUser).toList))
      )

    case GetAccount(accountId) ⇒
      val ref = sender
      getAccountState(accountId, state ⇒
        getTutorState(accountId, tutorState ⇒
          getRating(accountId, rating ⇒
            getPlace(rating.getOrElse(1400), place ⇒
              send(ref, AccountResponse(accountId, state, rating, tutorState, place))
            )
          )
        )
      )

    case GetAndUpdateAccountState(accountId, transform) ⇒
      val ref = sender
      getAccountState(accountId, state ⇒ {
        val newState = transform(state)
        updateAccountState(accountId, newState, () ⇒
          send(ref, AccountStateResponse(accountId, newState))
        )
      })

    case GetAndUpdateAccountStateAndRating(accountId, transform) ⇒
      val ref = sender
      getAccountState(accountId, state ⇒ {
        getRating(accountId, rating ⇒ {
          val (newState, newRating) = transform(state, rating)
          updateAccountState(accountId, newState, () ⇒
            updateRating(accountId, newRating, () ⇒
              getPlace(newRating, place ⇒
                send(ref, AccountStateAndRatingResponse(accountId, newState, newRating, place))
              )
            )
          )
        })
      })

    case UpdateTutorState(accountId, tutorState) ⇒
      write(
        "REPLACE INTO tutor_state (id,state) VALUES (?,?);",
        Seq(accountId.toByteArray, tutorState.toByteArray),
        () ⇒ {}
      )

    case UpdateUserInfo(accountId, userInfo) ⇒
      write(
        "REPLACE INTO user_info (id,info) VALUES (?,?);",
        Seq(accountId.toByteArray, userInfo.toByteArray),
        () ⇒ {}
      )
  }

  def getPlace(rating: Double, callback: Long ⇒ Unit): Unit =
    read(
      "SELECT COUNT(*) `place` " +
        "FROM ratings " +
        "WHERE rating > ?",
      Seq(rating),
      resultSet ⇒ {
        val place = resultSet.head("place").asInstanceOf[Long] + 1
        callback(place)
      }
    )

  def getRating(accountId: AccountId, callback: Option[Double] ⇒ Unit): Unit =
    read(
      "SELECT rating FROM ratings WHERE id=?;",
      Seq(accountId.toByteArray),
      resultSet ⇒
        if (resultSet.size == 0)
          callback(None)
        else if (resultSet.size == 1)
          callback(Some(rowDataToRating(resultSet.head)))
        else
          log.error("Get rating: invalid result rows count = " + resultSet.size)
    )

  def updateRating(accountId: AccountId, newRating: Double, callback: () ⇒ Unit): Unit =
    write(
      "REPLACE INTO ratings (id,rating) VALUES (?,?);",
      Seq(accountId.toByteArray, newRating),
      () ⇒ callback()
    )

  def updateAccountState(accountId: AccountId, newState: AccountStateDTO, callback: () ⇒ Unit): Unit =
    write(
      "REPLACE INTO account_state (id,state) VALUES (?,?);",
      Seq(accountId.toByteArray, newState.toByteArray),
      () ⇒ callback()
    )

  def getAccountState(accountId: AccountId, callback: Option[AccountStateDTO] ⇒ Unit): Unit =
    read(
      "SELECT state FROM account_state WHERE id=?;",
      Seq(accountId.toByteArray),
      resultSet ⇒
        if (resultSet.size == 0)
          callback(None)
        else if (resultSet.size == 1)
          callback(Some(rowDataToAccountState(resultSet.head)))
        else
          log.error("Get account state: invalid result rows count = " + resultSet.size)
    )

  def getTutorState(accountId: AccountId, callback: Option[TutorStateDTO] ⇒ Unit): Unit =
    read(
      "SELECT state FROM tutor_state WHERE id=?;",
      Seq(accountId.toByteArray),
      resultSet ⇒
        if (resultSet.size == 0)
          callback(None)
        else if (resultSet.size == 1)
          callback(Some(rowDataToTutorState(resultSet.head)))
        else
          log.error("Get tutor state: invalid result rows count = " + resultSet.size)
    )


  def rowDataToAccountState(row: RowData) = {
    val byteArray = row("state").asInstanceOf[Array[Byte]]
    AccountStateDTO.parseFrom(byteArray)
  }

  def rowDataToTutorState(row: RowData) = {
    val byteArray = row("state").asInstanceOf[Array[Byte]]
    TutorStateDTO.parseFrom(byteArray)
  }

  def rowDataToRating(row: RowData) =
    row("rating").asInstanceOf[Double]

  def rowDataToTopUser(rowData: RowData) = {
    val idByteArray = rowData(0).asInstanceOf[Array[Byte]]
    val id = AccountId.parseFrom(idByteArray)

    val rating = rowData(1).asInstanceOf[Double]

    val userInfoByteArray = rowData(2).asInstanceOf[Array[Byte]]
    val userInfo = UserInfoDTO.parseFrom(userInfoByteArray)

    TopUser(id, rating, userInfo)
  }

  def write(query: String, values: Seq[Any], onWrite: () ⇒ Unit): Unit =
    pool.sendPreparedStatement(query, values).map(
      queryResult ⇒
        if (queryResult.rowsAffected > 0)
          onWrite()
        else
          log.error("Invalid rows affected count " + queryResult)
    ) onFailure {
      case t: Throwable ⇒ log.error("Database error", t)
    }

  def read(query: String, values: Seq[Any], onRead: ResultSet ⇒ Unit): Unit =
    pool.sendPreparedStatement(query, values).map(
      queryResult ⇒ queryResult.rows match {
        case Some(resultSet) ⇒ onRead(resultSet)
        case None ⇒ log.error("Get none " + queryResult)
      }
    ) onFailure {
      case t: Throwable ⇒ log.error("Database error", t)
    }
}

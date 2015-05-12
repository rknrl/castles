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

  /** Ответом будет List[TopItem] */
  case object GetTop

  /** Ответом будет AccountStateResponse */
  case class GetAccountState(accountId: AccountId)

  case class AccountStateResponse(state: Option[AccountStateDTO])

  /** Ответом будет RatingResponse */
  case class GetRating(accountId: AccountId)

  case class RatingResponse(rating: Option[Double])

  /** Ответом будет TutorStateResponse */
  case class GetTutorState(accountId: AccountId)

  case class TutorStateResponse(tutorState: Option[TutorStateDTO])

  /** Ответом будет PlaceResponse */
  case class GetPlace(rating: Double)

  case class PlaceResponse(place: Long)

  /** Ответом будет AccountStateResponse */
  case class UpdateAccountState(accountId: AccountId, accountState: AccountStateDTO)

  /** Ответом будет RatingResponse */
  case class UpdateRating(accountId: AccountId, rating: Double)

  /** Без ответа */
  case class UpdateTutorState(accountId: AccountId, tutorState: TutorStateDTO)

  /** Без ответа */
  case class UpdateUserInfo(accountId: AccountId, userInfo: UserInfoDTO)

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

    case GetPlace(rating) ⇒
      val ref = sender
      read(
        "SELECT COUNT(*) `place` " +
          "FROM ratings " +
          "WHERE rating > ?",
        Seq(rating),
        resultSet ⇒ {
          val place = resultSet.head("place").asInstanceOf[Long] + 1
          send(ref, PlaceResponse(place))
        }
      )

    case UpdateAccountState(accountId, accountState) ⇒
      val ref = sender
      write(
        "REPLACE INTO account_state (id,state) VALUES (?,?);",
        Seq(accountId.toByteArray, accountState.toByteArray),
        () ⇒ send(ref, AccountStateResponse(Some(accountState)))
      )

    case UpdateRating(accountId, rating) ⇒
      val ref = sender
      write(
        "REPLACE INTO ratings (id,rating) VALUES (?,?);",
        Seq(accountId.toByteArray, rating),
        () ⇒ send(ref, RatingResponse(Some(rating)))
      )

    case UpdateUserInfo(accountId, userInfo) ⇒
      write(
        "REPLACE INTO user_info (id,userInfo) VALUES (?,?);",
        Seq(accountId.toByteArray, userInfo.toByteArray),
        () ⇒ {}
      )

    case UpdateTutorState(accountId, tutorState) ⇒
      write(
        "REPLACE INTO tutor_state (id,state) VALUES (?,?);",
        Seq(accountId.toByteArray, tutorState.toByteArray),
        () ⇒ {}
      )

    case GetRating(accountId) ⇒
      val ref = sender
      read(
        "SELECT rating FROM ratings WHERE id=?;",
        Seq(accountId.toByteArray),
        resultSet ⇒
          if (resultSet.size == 0)
            send(ref, RatingResponse(None))
          else if (resultSet.size == 1)
            send(ref, RatingResponse(Some(rowDataToRating(resultSet.head))))
          else
            log.error("Get rating: invalid result rows count = " + resultSet.size)
      )

    case GetAccountState(accountId) ⇒
      val ref = sender
      read(
        "SELECT state FROM account_state WHERE id=?;",
        Seq(accountId.toByteArray),
        resultSet ⇒
          if (resultSet.size == 0)
            send(ref, AccountStateResponse(None))
          else if (resultSet.size == 1)
            send(ref, AccountStateResponse(Some(rowDataToAccountState(resultSet.head))))
          else
            log.error("Get account state: invalid result rows count = " + resultSet.size)
      )

    case GetTutorState(accountId) ⇒
      val ref = sender
      read(
        "SELECT state FROM tutor_state WHERE id=?;",
        Seq(accountId.toByteArray),
        resultSet ⇒
          if (resultSet.size == 0)
            send(ref, TutorStateResponse(None))
          else if (resultSet.size == 1)
            send(ref, TutorStateResponse(Some(rowDataToTutorState(resultSet.head))))
          else
            log.error("Get tutor state: invalid result rows count = " + resultSet.size)
      )
  }

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

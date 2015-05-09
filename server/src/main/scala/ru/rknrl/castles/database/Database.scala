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

  /** Ответом будет AccountDeleted */
  case class DeleteAccount(accountId: AccountId)

  case class AccountDeleted(accountId: AccountId)

  /** Ответом будет AccountStateResponse или AccountNoExists */
  case class GetAccountState(accountId: AccountId)

  case class AccountStateResponse(accountId: AccountId, state: AccountStateDTO, rating: Option[Double])

  case object AccountNoExists

  /** Ответом будет TutorStateResponse */
  case class GetTutorState(accountId: AccountId)

  case class TutorStateResponse(accountId: AccountId, tutorState: TutorStateDTO)

  /** Ответом будет AccountStateResponse */
  case class Insert(accountId: AccountId, accountState: AccountStateDTO, userInfo: UserInfoDTO, tutorState: TutorStateDTO, rating: Double)

  /** Ответом будет AccountStateResponse */
  case class UpdateAccountState(accountId: AccountId, accountState: AccountStateDTO, rating: Double)

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
        "SELECT account_state.id,coalesce(ratings.rating,0) rating,userInfo " +
          "FROM account_state " +
          "LEFT JOIN ratings ON ratings.id=account_state.id " +
          "GROUP BY ratings.id " +
          "ORDER BY coalesce(ratings.rating,0) DESC " +
          "LIMIT 5;",
        Seq.empty,
        resultSet ⇒ send(ref, Top(resultSet.map(rowDataToTopUser).toList))
      )

    case DeleteAccount(accountId) ⇒
      val ref = sender
      write(
        "DELETE FROM account_state WHERE id=?;",
        Seq(accountId.toByteArray),
        () ⇒ write(
          "DELETE FROM tutor_state WHERE id=?;",
          Seq(accountId.toByteArray),
          () ⇒ send(ref, AccountDeleted(accountId))
        )
      )

    case Insert(accountId, accountState, userInfo, tutorState, rating) ⇒
      val ref = sender
      write(
        "INSERT INTO account_state (id,state,userInfo) VALUES (?,?,?);",
        Seq(accountId.toByteArray, accountState.toByteArray, userInfo.toByteArray),
        () ⇒
          write(
            "INSERT INTO tutor_state (id,state) VALUES (?,?);",
            Seq(accountId.toByteArray, tutorState.toByteArray),
            () ⇒ send(ref, AccountStateResponse(accountId, accountState, Some(rating)))
          )
      )

    case UpdateAccountState(accountId, accountState, rating) ⇒
      val ref = sender
      write(
        "UPDATE account_state SET state=? WHERE id=?;",
        Seq(accountState.toByteArray, accountId.toByteArray),
        () ⇒
          write(
            "REPLACE INTO ratings (id,rating) VALUES (?,?);",
            Seq(accountId.toByteArray, rating),
            () ⇒ send(ref, AccountStateResponse(accountId, accountState, Some(rating)))
          )
      )

    case UpdateUserInfo(accountId, userInfo) ⇒
      write(
        "UPDATE account_state SET userInfo=? WHERE id=?;",
        Seq(userInfo.toByteArray, accountId.toByteArray),
        () ⇒ {}
      )

    case UpdateTutorState(accountId, tutorState) ⇒
      write(
        "UPDATE tutor_state SET state=? WHERE id=?;",
        Seq(tutorState.toByteArray, accountId.toByteArray),
        () ⇒ {}
      )

    case GetAccountState(accountId) ⇒
      val ref = sender
      read(
        "SELECT state FROM account_state WHERE id=?;",
        Seq(accountId.toByteArray),
        resultSet ⇒
          if (resultSet.size == 0)
            send(ref, AccountNoExists)
          else if (resultSet.size == 1) {
            val accountState = rowDataToAccountState(resultSet.head)
            read(
              "SELECT rating FROM ratings WHERE id=?;",
              Seq(accountId.toByteArray),
              resultSet ⇒
                if (resultSet.size == 0)
                  send(ref, AccountStateResponse(accountId, accountState, rating = None))
                else if (resultSet.size == 1)
                  send(ref, AccountStateResponse(accountId, accountState, rating = Some(resultSet.head.asInstanceOf[Double])))
                else
                  log.error("Get rating: invalid result rows count = " + resultSet.size)
            )
          } else
            log.error("Get account state: invalid result rows count = " + resultSet.size)
      )

    case GetTutorState(accountId) ⇒
      val ref = sender
      read(
        "SELECT state FROM tutor_state WHERE id=?;",
        Seq(accountId.toByteArray),
        resultSet ⇒
          if (resultSet.size == 1)
            send(ref, TutorStateResponse(accountId, rowDataToTutorState(resultSet.head)))
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
        if (queryResult.rowsAffected == 1)
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

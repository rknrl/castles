//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.database

import akka.actor.{Actor, Props}
import com.github.mauricio.async.db.mysql.pool.MySQLConnectionFactory
import com.github.mauricio.async.db.pool.{ConnectionPool, PoolConfiguration}
import com.github.mauricio.async.db.util.ExecutorServiceUtils.CachedExecutionContext
import com.github.mauricio.async.db.{Configuration, ResultSet, RowData}
import protos._
import ru.rknrl.castles.database.Database._
import ru.rknrl.castles.database.DatabaseTransaction.{Request, Response}
import ru.rknrl.castles.matchmaking.{Top, TopUser}
import ru.rknrl.logging.ShortActorLogging

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

  def props(config: DbConfiguration) = Props(classOf[Database], config)

  case class GetTop(weekNumber: Int)

  case class GetAccountState(accountId: AccountId) extends Request

  case class AccountStateResponse(accountId: AccountId, state: Option[AccountState]) extends Response

  case class UpdateAccountState(accountId: AccountId, newState: AccountState) extends Request


  case class GetRating(accountId: AccountId, weekNumber: Int) extends Request

  case class RatingResponse(accountId: AccountId, weekNumber: Int, rating: Option[Double]) extends Response

  case class UpdateRating(accountId: AccountId, weekNumber: Int, newRating: Double, userInfoDTO: UserInfo) extends Request


  case class GetTutorState(accountId: AccountId) extends Request

  case class TutorStateResponse(accountId: AccountId, tutorState: Option[TutorState]) extends Response

  case class UpdateTutorState(accountId: AccountId, newTutorState: TutorState) extends Request


  case class GetPlace(rating: Double, weekNumber: Int)

  case class PlaceResponse(rating: Double, weekNumber: Int, place: Long)


  case class UpdateUserInfo(accountId: AccountId, userInfo: UserInfo) extends Request

  case class UserInfoResponse(accountId: AccountId, userInfo: Option[UserInfo]) extends Response

}

class Database(configuration: DbConfiguration) extends Actor with ShortActorLogging {

  val factory = new MySQLConnectionFactory(configuration.configuration)
  val pool = new ConnectionPool(factory, configuration.poolConfiguration)

  override def receive: Receive = logged {
    case GetTop(weekNumber) ⇒
      val ref = sender
      read(
        "SELECT ratings.id, ratings.rating, userInfo " +
          "FROM ratings " +
          "LEFT JOIN user_info ON user_info.id=ratings.id " +
          "WHERE ratings.weekNumber=? " +
          "GROUP BY ratings.id " +
          "ORDER BY ratings.rating DESC " +
          "LIMIT 5;",
        Seq(weekNumber),
        resultSet ⇒ send(ref, Top(resultSet.map(rowDataToTopUser).toList, weekNumber))
      )

    case GetPlace(rating, weekNumber) ⇒
      val ref = sender
      read(
        "SELECT COUNT(*) `place` " +
          "FROM ratings " +
          "WHERE weekNumber = ? AND rating > ?",
        Seq(weekNumber, rating),
        resultSet ⇒ {
          val place = resultSet.head("place").asInstanceOf[Long] + 1
          send(ref, PlaceResponse(rating, weekNumber, place))
        }
      )

    case GetRating(accountId, weekNumber) ⇒
      val ref = sender
      read(
        "SELECT rating FROM ratings WHERE weekNumber = ? AND id=?;",
        Seq(weekNumber, accountId.toByteArray),
        resultSet ⇒
          if (resultSet.isEmpty)
            send(ref, RatingResponse(accountId, weekNumber, None))
          else if (resultSet.size == 1)
            send(ref, RatingResponse(accountId, weekNumber, Some(rowDataToRating(resultSet.head))))
          else
            log.error("Get rating: invalid result rows count = " + resultSet.size)
      )

    case UpdateRating(accountId, weekNumber, newRating, userInfo) ⇒
      val ref = sender
      write(
        "REPLACE INTO ratings (id,weekNumber,rating) VALUES (?,?,?);",
        Seq(accountId.toByteArray, weekNumber, newRating),
        () ⇒ send(ref, RatingResponse(accountId, weekNumber, Some(newRating)))
      )

    case UpdateAccountState(accountId, newState) ⇒
      val ref = sender
      write(
        "REPLACE INTO account_state (id,state) VALUES (?,?);",
        Seq(accountId.toByteArray, newState.toByteArray),
        () ⇒ send(ref, AccountStateResponse(accountId, Some(newState)))
      )

    case GetAccountState(accountId) ⇒
      val ref = sender
      read(
        "SELECT state FROM account_state WHERE id=?;",
        Seq(accountId.toByteArray),
        resultSet ⇒
          if (resultSet.isEmpty)
            send(ref, AccountStateResponse(accountId, None))
          else if (resultSet.size == 1)
            send(ref, AccountStateResponse(accountId, Some(rowDataToAccountState(resultSet.head))))
          else
            log.error("Get account state: invalid result rows count = " + resultSet.size)
      )

    case GetTutorState(accountId) ⇒
      val ref = sender
      read(
        "SELECT state FROM tutor_state WHERE id=?;",
        Seq(accountId.toByteArray),
        resultSet ⇒
          if (resultSet.isEmpty)
            send(ref, TutorStateResponse(accountId, None))
          else if (resultSet.size == 1)
            send(ref, TutorStateResponse(accountId, Some(rowDataToTutorState(resultSet.head))))
          else
            log.error("Get tutor state: invalid result rows count = " + resultSet.size)
      )

    case UpdateTutorState(accountId, tutorState) ⇒
      val ref = sender
      write(
        "REPLACE INTO tutor_state (id,state) VALUES (?,?);",
        Seq(accountId.toByteArray, tutorState.toByteArray),
        () ⇒ send(ref, TutorStateResponse(accountId, Some(tutorState)))
      )

    case UpdateUserInfo(accountId, userInfo) ⇒
      val ref = sender
      write(
        "REPLACE INTO user_info (id,userInfo) VALUES (?,?);",
        Seq(accountId.toByteArray, userInfo.toByteArray),
        () ⇒ send(ref, UserInfoResponse(accountId, Some(userInfo)))
      )
  }

  def rowDataToAccountState(row: RowData) = {
    val byteArray = row("state").asInstanceOf[Array[Byte]]
    AccountState.parseFrom(byteArray)
  }

  def rowDataToTutorState(row: RowData) = {
    val byteArray = row("state").asInstanceOf[Array[Byte]]
    TutorState.parseFrom(byteArray)
  }

  def rowDataToRating(row: RowData) =
    row("rating").asInstanceOf[Double]

  def rowDataToTopUser(rowData: RowData) = {
    val idByteArray = rowData(0).asInstanceOf[Array[Byte]]
    val id = AccountId.parseFrom(idByteArray)

    val rating = rowData(1).asInstanceOf[Double]

    val userInfo = if (rowData(2) == null)
      UserInfo(id)
    else {
      val userInfoByteArray = rowData(2).asInstanceOf[Array[Byte]]
      UserInfo.parseFrom(userInfoByteArray)
    }

    TopUser(id, rating, userInfo)
  }

  def write(query: String, values: Seq[Any], onWrite: () ⇒ Unit): Unit =
    pool.sendPreparedStatement(query, values) map (
      queryResult ⇒
        if (queryResult.rowsAffected > 0)
          onWrite()
        else
          log.error("Invalid rows affected count " + queryResult)
      ) onFailure {
      case t: Throwable ⇒ log.error("Database error", t)
    }

  def read(query: String, values: Seq[Any], onRead: ResultSet ⇒ Unit): Unit =
    pool.sendPreparedStatement(query, values) map (
      queryResult ⇒ queryResult.rows match {
        case Some(resultSet) ⇒ onRead(resultSet)
        case None ⇒ log.error("Get none " + queryResult)
      }
      ) onFailure {
      case t: Throwable ⇒ log.error("Database error", t)
    }
}

//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.database

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.Patterns
import com.github.mauricio.async.db.mysql.pool.MySQLConnectionFactory
import com.github.mauricio.async.db.pool.{ConnectionPool, PoolConfiguration}
import com.github.mauricio.async.db.util.ExecutorServiceUtils.CachedExecutionContext
import com.github.mauricio.async.db.{Configuration, ResultSet, RowData}
import protos._
import ru.rknrl.castles.database.Database.{AccountStateResponse, _}
import ru.rknrl.castles.database.DatabaseTransaction.{AccountStateAndRatingResponse, GetAndUpdateAccountStateAndRating, _}
import ru.rknrl.castles.matchmaking.{Top, TopUser}
import ru.rknrl.logging.ShortActorLogging

import scala.concurrent.Future
import scala.concurrent.duration._

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

  def props(config: DbConfiguration, calendar: Calendar) = Props(classOf[Database], config, calendar)

  case class GetTop(weekNumber: Int)

  case class GetAccountState(accountId: AccountId)

  case class AccountStateResponse(accountId: AccountId, state: Option[AccountState])

  case class UpdateAccountState(accountId: AccountId, newState: AccountState)


  case class GetRating(accountId: AccountId, weekNumber: Int)

  case class RatingResponse(accountId: AccountId, weekNumber: Int, rating: Option[Double])

  case class UpdateRating(accountId: AccountId, weekNumber: Int, newRating: Double, userInfoDTO: UserInfo)


  case class GetTutorState(accountId: AccountId)

  case class TutorStateResponse(accountId: AccountId, tutorState: Option[TutorState])

  case class UpdateTutorState(accountId: AccountId, newTutorState: TutorState)


  case class GetPlace(rating: Double, weekNumber: Int)

  case class PlaceResponse(rating: Double, weekNumber: Int, place: Long)


  case class UpdateUserInfo(accountId: AccountId, userInfo: UserInfo)

  case class UserInfoResponse(accountId: AccountId, userInfo: Option[UserInfo])

}

object DatabaseTransaction {

  case class GetAccount(accountId: AccountId)

  case class AccountResponse(accountId: AccountId,
                             state: Option[AccountState],
                             rating: Option[Double],
                             tutorState: Option[TutorState],
                             top: Top,
                             place: Option[Long],
                             lastWeekPlace: Option[Long],
                             lastWeekTop: Top)

  case class GetAndUpdateAccountState(accountId: AccountId, transform: Option[AccountState] ⇒ AccountState)

  case class AccountStateResponse(accountId: AccountId, state: AccountState)

  case class GetAndUpdateAccountStateAndRating(accountId: AccountId, transform: (Option[AccountState], Option[Double]) ⇒ (AccountState, Double), userInfo: UserInfo)

  case class AccountStateAndRatingResponse(accountId: AccountId, state: AccountState, rating: Double, place: Long, top: Top)

}

class Database(configuration: DbConfiguration, calendar: Calendar) extends Actor with ShortActorLogging {

  val factory = new MySQLConnectionFactory(configuration.configuration)
  val pool = new ConnectionPool(factory, configuration.poolConfiguration)

  override def receive: Receive = logged {
    case GetAccount(accountId) ⇒
      val ref = sender
      val currentWeek = calendar.getCurrentWeek
      val lastWeek = currentWeek - 1

      getAccountState(accountId, state ⇒
        getTutorState(accountId, tutorState ⇒
          getRating(currentWeek, accountId, rating ⇒
            getOptionPlace(currentWeek, rating, place ⇒
              getTop(currentWeek, top ⇒
                getRating(lastWeek, accountId, lastWeekRating ⇒
                  getOptionPlace(lastWeek, lastWeekRating, lastWeekPlace ⇒
                    getTop(lastWeek, lastWeekTop ⇒
                      send(ref, AccountResponse(accountId, state, rating, tutorState, top, place, lastWeekPlace, lastWeekTop))
                    )
                  )
                )
              )
            )
          )
        )
      )

    case GetAndUpdateAccountState(accountId, transform) ⇒
      val ref = sender
      getAccountState(accountId, state ⇒ {
        val newState = transform(state)
        updateAccountState(accountId, newState, () ⇒
          send(ref, DatabaseTransaction.AccountStateResponse(accountId, newState))
        )
      })

    case GetAndUpdateAccountStateAndRating(accountId, transform, userInfo) ⇒
      val ref = sender
      val currentWeek = calendar.getCurrentWeek
      getAccountState(accountId, state ⇒ {
        getRating(currentWeek, accountId, rating ⇒ {
          val (newState, newRating) = transform(state, rating)
          updateAccountState(accountId, newState, () ⇒
            updateRating(currentWeek, accountId, newRating, userInfo, () ⇒
              getPlace(currentWeek, newRating, place ⇒
                getTop(currentWeek, top ⇒
                  send(ref, AccountStateAndRatingResponse(accountId, newState, newRating, place, top))
                )
              )
            )
          )
        })
      })

    //

    case GetTop(weekNumber) ⇒
      getTop(sender, weekNumber)

    case GetPlace(rating, weekNumber) ⇒
      getPlace(sender, rating, weekNumber)

    case GetRating(accountId, weekNumber) ⇒
      getRating(sender, accountId, weekNumber)

    case UpdateRating(accountId, weekNumber, newRating, userInfo) ⇒
      replaceRating(sender, accountId, weekNumber, newRating, userInfo)

    case UpdateAccountState(accountId, newState) ⇒
      replaceAccountState(sender, accountId, newState)

    case GetAccountState(accountId) ⇒
      getAccountState(sender, accountId)

    case GetTutorState(accountId) ⇒
      getTutorState(sender, accountId)

    case UpdateTutorState(accountId, tutorState) ⇒
      replaceTutorState(sender, accountId, tutorState)

    case UpdateUserInfo(accountId, userInfo) ⇒
      replaceUserInfo(sender, accountId, userInfo)
  }

  def getTop(sender: ActorRef, weekNumber: Int): Unit =
    read(
      "SELECT ratings.id, ratings.rating, userInfo " +
        "FROM ratings " +
        "LEFT JOIN user_info ON user_info.id=ratings.id " +
        "WHERE ratings.weekNumber=? " +
        "GROUP BY ratings.id " +
        "ORDER BY ratings.rating DESC " +
        "LIMIT 5;",
      Seq(weekNumber),
      resultSet ⇒ send(sender, Top(resultSet.map(rowDataToTopUser).toList, weekNumber))
    )

  def getPlace(sender: ActorRef, rating: Double, weekNumber: Int): Unit =
    read(
      "SELECT COUNT(*) `place` " +
        "FROM ratings " +
        "WHERE weekNumber = ? AND rating > ?",
      Seq(weekNumber, rating),
      resultSet ⇒ {
        val place = resultSet.head("place").asInstanceOf[Long] + 1
        send(sender, PlaceResponse(rating, weekNumber, place))
      }
    )

  def getRating(sender: ActorRef, accountId: AccountId, weekNumber: Int): Unit =
    read(
      "SELECT rating FROM ratings WHERE weekNumber = ? AND id=?;",
      Seq(weekNumber, accountId.toByteArray),
      resultSet ⇒
        if (resultSet.isEmpty)
          send(sender, RatingResponse(accountId, weekNumber, None))
        else if (resultSet.size == 1)
          send(sender, RatingResponse(accountId, weekNumber, Some(rowDataToRating(resultSet.head))))
        else
          log.error("Get rating: invalid result rows count = " + resultSet.size)
    )

  def replaceRating(sender: ActorRef, accountId: AccountId, weekNumber: Int, newRating: Double, userInfo: UserInfo): Unit =
    write(
      "REPLACE INTO ratings (id,weekNumber,rating) VALUES (?,?,?);",
      Seq(accountId.toByteArray, weekNumber, newRating),
      () ⇒ send(sender, RatingResponse(accountId, weekNumber, Some(newRating)))
    )

  def replaceAccountState(sender: ActorRef, accountId: AccountId, newState: AccountState): Unit =
    write(
      "REPLACE INTO account_state (id,state) VALUES (?,?);",
      Seq(accountId.toByteArray, newState.toByteArray),
      () ⇒ send(sender, AccountStateResponse(accountId, Some(newState)))
    )

  def getAccountState(accountId: AccountId): Future[AccountStateResponse] =
    read(
      "SELECT state FROM account_state WHERE id=?;",
      Seq(accountId.toByteArray)
    ) flatMap { resultSet ⇒
      if (resultSet.isEmpty)
        Future.successful(AccountStateResponse(accountId, None))
      else if (resultSet.size == 1)
        Future.successful(AccountStateResponse(accountId, Some(rowDataToAccountState(resultSet.head))))
      else
        Future.failed(new Exception("Get account state: invalid result rows count = " + resultSet.size))
    }

  def getTutorState(accountId: AccountId): Future[TutorStateResponse] =
    read(
      "SELECT state FROM tutor_state WHERE id=?;",
      Seq(accountId.toByteArray)
    ) flatMap {      resultSet ⇒
      if (resultSet.isEmpty)
        Future.successful(TutorStateResponse(accountId, None))
      else if (resultSet.size == 1)
        Future.successful(TutorStateResponse(accountId, Some(rowDataToTutorState(resultSet.head))))
      else
        Future.failed(new Exception("Get tutor state: invalid result rows count = " + resultSet.size))
    }

  def replaceTutorState(accountId: AccountId, tutorState: TutorState): Future[TutorStateResponse] =
    write(
      "REPLACE INTO tutor_state (id,state) VALUES (?,?);",
      Seq(accountId.toByteArray, tutorState.toByteArray)
    ) flatMap { _ ⇒
      Future.successful(TutorStateResponse(accountId, Some(tutorState)))
    }

  def replaceUserInfo(accountId: AccountId, userInfo: UserInfo): Future[UserInfoResponse] =
    write(
      "REPLACE INTO user_info (id,userInfo) VALUES (?,?);",
      Seq(accountId.toByteArray, userInfo.toByteArray)
    ) flatMap { _ ⇒
      Future.successful(UserInfoResponse(accountId, Some(userInfo)))
    }

  //

  def answer(future: Future[Any]): Unit = {
    val ref = sender
    future onSuccess {
      case result ⇒ ref ! result
    }
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

  def write(query: String, values: Seq[Any]): Future[Unit] =
    pool.sendPreparedStatement(query, values) flatMap (
      queryResult ⇒
        if (queryResult.rowsAffected > 0)
          Future.successful()
        else
          Future.failed(new Exception("Invalid rows affected count " + queryResult))
      )

  def read(query: String, values: Seq[Any]): Future[ResultSet] =
    pool.sendPreparedStatement(query, values) flatMap (
      queryResult ⇒ queryResult.rows match {
        case Some(resultSet) ⇒ Future.successful(resultSet)
        case None ⇒ Future.failed(new Exception("Get none " + queryResult))
      }
      )

  //

  val timeout = 10 seconds

  def getTop(weekNumber: Int, callback: Top ⇒ Unit): Unit = {
    val msg = GetTop(weekNumber)
    Patterns.ask(database, msg, timeout) map {
      case top: Top ⇒ callback(top)
    } onFailure {
      case t: Throwable ⇒ log.error(msg.toString, t)
    }
  }

  def getRating(weekNumber: Int, accountId: AccountId, callback: Option[Double] ⇒ Unit): Unit = {
    val msg = GetRating(accountId, weekNumber)
    Patterns.ask(database, msg, timeout) map {
      case RatingResponse(accountId, weekNumber, rating) ⇒ callback(rating)
    } onFailure {
      case t: Throwable ⇒ log.error(msg.toString, t)
    }
  }

  def getOptionPlace(weekNumber: Int, rating: Option[Double], callback: Option[Long] ⇒ Unit): Unit =
    if (rating.isDefined)
      getPlace(weekNumber, rating.get, place ⇒ callback(Some(place)))
    else
      callback(None)

  def getPlace(weekNumber: Int, rating: Double, callback: Long ⇒ Unit): Unit = {
    val msg = GetPlace(rating, weekNumber)
    Patterns.ask(database, msg, timeout) map {
      case PlaceResponse(rating, weekNumber, place) ⇒ callback(place)
    } onFailure {
      case t: Throwable ⇒ log.error(msg.toString, t)
    }
  }

  def updateRating(weekNumber: Int, accountId: AccountId, newRating: Double, userInfo: UserInfo, callback: () ⇒ Unit): Unit = {
    val msg = UpdateRating(accountId, weekNumber, newRating, userInfo)
    Patterns.ask(database, msg, timeout) map {
      case RatingResponse(accountId, weekNumber, rating) ⇒ callback()
    } onFailure {
      case t: Throwable ⇒ log.error(msg.toString, t)
    }
  }

  def getAccountState(accountId: AccountId, callback: Option[AccountState] ⇒ Unit): Unit = {
    val msg = GetAccountState(accountId)
    Patterns.ask(database, msg, timeout) map {
      case Database.AccountStateResponse(accountId, state) ⇒ callback(state)
    } onFailure {
      case t: Throwable ⇒ log.error(msg.toString, t)
    }
  }

  def updateAccountState(accountId: AccountId, newState: AccountState, callback: () ⇒ Unit): Unit = {
    val msg = UpdateAccountState(accountId, newState)
    Patterns.ask(database, msg, timeout) map {
      case Database.AccountStateResponse(accountId, state) ⇒ callback()
    } onFailure {
      case t: Throwable ⇒ log.error(msg.toString, t)
    }
  }

  def getTutorState(accountId: AccountId, callback: Option[TutorState] ⇒ Unit): Unit = {
    val msg = GetTutorState(accountId)
    Patterns.ask(database, msg, timeout) map {
      case Database.TutorStateResponse(accountId, state) ⇒ callback(state)
    } onFailure {
      case t: Throwable ⇒ log.error(msg.toString, t)
    }
  }

}

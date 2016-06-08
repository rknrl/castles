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
import com.github.mauricio.async.db.{Configuration, Connection, ResultSet, RowData}
import protos._
import ru.rknrl.castles.database.Database.{AccountStateAndRatingResponse, AccountStateResponse, GetAndUpdateAccountStateAndRating, _}
import ru.rknrl.castles.matchmaking.{Top, TopUser}
import ru.rknrl.logging.ShortActorLogging

import scala.concurrent.Future

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

  case class AccountStateUpdated(accountId: AccountId, state: AccountState)

  case class GetAndUpdateAccountStateAndRating(accountId: AccountId, transform: (Option[AccountState], Option[Double]) ⇒ (AccountState, Double), userInfo: UserInfo)

  case class AccountStateAndRatingResponse(accountId: AccountId, state: AccountState, rating: Double, place: Long, top: Top)

}

class Database(configuration: DbConfiguration, calendar: Calendar) extends Actor with ShortActorLogging {
  val factory = new MySQLConnectionFactory(configuration.configuration)
  val pool = new ConnectionPool(factory, configuration.poolConfiguration)

  def receive: Receive = logged {
    case GetAccount(accountId) ⇒
      val currentWeek = calendar.getCurrentWeek
      val lastWeek = currentWeek - 1

      answer { implicit connection ⇒
        getAccountState(accountId) flatMap { state ⇒
          getTutorState(accountId) flatMap { tutorState ⇒
            getRating(accountId, currentWeek) flatMap { rating ⇒
              getOptionPlace(rating, currentWeek) flatMap { place ⇒
                getTop(currentWeek) flatMap { top ⇒
                  getRating(accountId, lastWeek) flatMap { lastWeekRating ⇒
                    getOptionPlace(lastWeekRating, lastWeek) flatMap { lastWeekPlace ⇒
                      getTop(lastWeek) flatMap { lastWeekTop ⇒
                        Future.successful(AccountResponse(accountId, state, rating, tutorState, top, place, lastWeekPlace, lastWeekTop))
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }

    case GetAndUpdateAccountState(accountId, transform) ⇒
      answer { implicit connection ⇒
        getAccountState(accountId) flatMap { state ⇒
          val newState = transform(state)
          replaceAccountState(accountId, newState) flatMap { _ ⇒
            Future.successful(AccountStateUpdated(accountId, newState))
          }
        }
      }

    case GetAndUpdateAccountStateAndRating(accountId, transform, userInfo) ⇒
      val currentWeek = calendar.getCurrentWeek
      answer { implicit connection ⇒
        getAccountState(accountId) flatMap { state ⇒
          getRating(accountId, currentWeek) flatMap { rating ⇒
            val (newState, newRating) = transform(state, rating)
            replaceAccountState(accountId, newState) flatMap { _ ⇒
              replaceRating(accountId, currentWeek, newRating, userInfo) flatMap { _ ⇒
                getPlace(newRating, currentWeek) flatMap { place ⇒
                  getTop(currentWeek) flatMap { top ⇒
                    Future.successful(AccountStateAndRatingResponse(accountId, newState, newRating, place, top))
                  }
                }
              }
            }
          }
        }
      }

    case GetTop(weekNumber) ⇒
      answer(implicit connection ⇒ getTop(weekNumber))

    case GetPlace(rating, weekNumber) ⇒
      answer(implicit connection ⇒ getPlaceResponse(rating, weekNumber))

    case GetRating(accountId, weekNumber) ⇒
      answer(implicit connection ⇒ getRatingResponse(accountId, weekNumber))

    case UpdateRating(accountId, weekNumber, newRating, userInfo) ⇒
      answer(implicit connection ⇒ replaceRating(accountId, weekNumber, newRating, userInfo))

    case UpdateAccountState(accountId, newState) ⇒
      answer(implicit connection ⇒ replaceAccountState(accountId, newState))

    case GetAccountState(accountId) ⇒
      answer(implicit connection ⇒ getAccountStateResponse(accountId))

    case GetTutorState(accountId) ⇒
      answer(implicit connection ⇒ getTutorStateResponse(accountId))

    case UpdateTutorState(accountId, tutorState) ⇒
      answer(implicit connection ⇒ replaceTutorState(accountId, tutorState))

    case UpdateUserInfo(accountId, userInfo) ⇒
      answer(implicit connection ⇒ replaceUserInfo(accountId, userInfo))
  }

  def getTop(weekNumber: Int)(implicit connection: Connection): Future[Top] =
    read(
      "SELECT ratings.id, ratings.rating, userInfo " +
        "FROM ratings " +
        "LEFT JOIN user_info ON user_info.id=ratings.id " +
        "WHERE ratings.weekNumber=? " +
        "GROUP BY ratings.id " +
        "ORDER BY ratings.rating DESC " +
        "LIMIT 5;",
      Seq(weekNumber)
    ) flatMap { resultSet ⇒
      Future.successful(Top(resultSet.map(rowDataToTopUser).toList, weekNumber))
    }

  def getPlaceResponse(rating: Double, weekNumber: Int)(implicit connection: Connection): Future[PlaceResponse] =
    getPlace(rating, weekNumber) flatMap { place ⇒
      Future.successful(PlaceResponse(rating, weekNumber, place))
    }

  def getOptionPlace(rating: Option[Double], weekNumber: Int)(implicit connection: Connection): Future[Option[Long]] =
    if (rating.isDefined)
      getPlace(rating.get, weekNumber) flatMap { place ⇒
        Future.successful(Some(place))
      }
    else
      Future.successful(None)

  def getPlace(rating: Double, weekNumber: Int)(implicit connection: Connection): Future[Long] =
    read(
      "SELECT COUNT(*) `place` " +
        "FROM ratings " +
        "WHERE weekNumber = ? AND rating > ?",
      Seq(weekNumber, rating)
    ) flatMap { resultSet ⇒
      val place = resultSet.head("place").asInstanceOf[Long] + 1
      Future.successful(place)
    }

  def getRatingResponse(accountId: AccountId, weekNumber: Int)(implicit connection: Connection): Future[RatingResponse] =
    getRating(accountId, weekNumber) flatMap { rating ⇒
      Future.successful(RatingResponse(accountId, weekNumber, rating))
    }

  def getRating(accountId: AccountId, weekNumber: Int)(implicit connection: Connection): Future[Option[Double]] =
    read(
      "SELECT rating FROM ratings WHERE weekNumber = ? AND id=?;",
      Seq(weekNumber, accountId.toByteArray)
    ) flatMap { resultSet ⇒
      if (resultSet.isEmpty)
        Future.successful(None)
      else if (resultSet.size == 1)
        Future.successful(Some(rowDataToRating(resultSet.head)))
      else
        Future.failed(new Exception("Get rating: invalid result rows count = " + resultSet.size))
    }

  def replaceRating(accountId: AccountId, weekNumber: Int, newRating: Double, userInfo: UserInfo)(implicit connection: Connection): Future[RatingResponse] =
    write(
      "REPLACE INTO ratings (id,weekNumber,rating) VALUES (?,?,?);",
      Seq(accountId.toByteArray, weekNumber, newRating)
    ) flatMap { _ ⇒
      Future.successful(RatingResponse(accountId, weekNumber, Some(newRating)))
    }

  def replaceAccountState(accountId: AccountId, newState: AccountState)(implicit connection: Connection): Future[AccountStateResponse] =
    write(
      "REPLACE INTO account_state (id,state) VALUES (?,?);",
      Seq(accountId.toByteArray, newState.toByteArray)
    ) flatMap { _ ⇒
      Future.successful(AccountStateResponse(accountId, Some(newState)))
    }

  def getAccountStateResponse(accountId: AccountId)(implicit connection: Connection): Future[AccountStateResponse] =
    getAccountState(accountId) flatMap { accountState ⇒
      Future.successful(AccountStateResponse(accountId, accountState))
    }

  def getAccountState(accountId: AccountId)(implicit connection: Connection): Future[Option[AccountState]] =
    read(
      "SELECT state FROM account_state WHERE id=?;",
      Seq(accountId.toByteArray)
    ) flatMap { resultSet ⇒
      if (resultSet.isEmpty)
        Future.successful(None)
      else if (resultSet.size == 1)
        Future.successful(Some(rowDataToAccountState(resultSet.head)))
      else
        Future.failed(new Exception("Get account state: invalid result rows count = " + resultSet.size))
    }

  def getTutorStateResponse(accountId: AccountId)(implicit connection: Connection): Future[TutorStateResponse] =
    getTutorState(accountId) flatMap { tutorState ⇒
      Future.successful(TutorStateResponse(accountId, tutorState))
    }

  def getTutorState(accountId: AccountId)(implicit connection: Connection): Future[Option[TutorState]] =
    read(
      "SELECT state FROM tutor_state WHERE id=?;",
      Seq(accountId.toByteArray)
    ) flatMap { resultSet ⇒
      if (resultSet.isEmpty)
        Future.successful(None)
      else if (resultSet.size == 1)
        Future.successful(Some(rowDataToTutorState(resultSet.head)))
      else
        Future.failed(new Exception("Get tutor state: invalid result rows count = " + resultSet.size))
    }

  def replaceTutorState(accountId: AccountId, tutorState: TutorState)(implicit connection: Connection): Future[TutorStateResponse] =
    write(
      "REPLACE INTO tutor_state (id,state) VALUES (?,?);",
      Seq(accountId.toByteArray, tutorState.toByteArray)
    ) flatMap { _ ⇒
      Future.successful(TutorStateResponse(accountId, Some(tutorState)))
    }

  def replaceUserInfo(accountId: AccountId, userInfo: UserInfo)(implicit connection: Connection): Future[UserInfoResponse] =
    write(
      "REPLACE INTO user_info (id,userInfo) VALUES (?,?);",
      Seq(accountId.toByteArray, userInfo.toByteArray)
    ) flatMap { _ ⇒
      Future.successful(UserInfoResponse(accountId, Some(userInfo)))
    }

  // base

  def answer(f: Connection ⇒ Future[Any]): Unit = {
    val ref = sender
    val future = pool.inTransaction(f)
    future onSuccess {
      case result ⇒ ref ! result
    }
  }

  def write(query: String, values: Seq[Any])(implicit connection: Connection): Future[Unit] =
    connection.sendPreparedStatement(query, values) flatMap (
      queryResult ⇒
        if (queryResult.rowsAffected > 0)
          Future.successful()
        else
          Future.failed(new Exception("Invalid rows affected count " + queryResult))
      )

  def read(query: String, values: Seq[Any])(implicit connection: Connection): Future[ResultSet] =
    connection.sendPreparedStatement(query, values) flatMap (
      queryResult ⇒ queryResult.rows match {
        case Some(resultSet) ⇒ Future.successful(resultSet)
        case None ⇒ Future.failed(new Exception("Get none " + queryResult))
      }
      )

  // serialize

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
}

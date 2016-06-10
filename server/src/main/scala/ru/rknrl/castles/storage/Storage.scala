//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.storage

import akka.actor.{Actor, Props}
import com.github.mauricio.async.db.mysql.pool.MySQLConnectionFactory
import com.github.mauricio.async.db.pool.{ConnectionPool, PoolConfiguration}
import com.github.mauricio.async.db.util.ExecutorServiceUtils.CachedExecutionContext
import com.github.mauricio.async.db.{Configuration, Connection, ResultSet, RowData}
import protos._
import ru.rknrl.castles.matchmaking.{Top, TopUser}
import ru.rknrl.castles.storage.Parsers._
import ru.rknrl.castles.storage.Storage.{AccountStateAndRatingUpdated, GetAndUpdateAccountStateAndRating, _}
import ru.rknrl.logging.ShortActorLogging

import scala.concurrent.Future
import scala.concurrent.Future.{failed, successful}

class StorageConfig(username: String,
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

object Storage {

  def props(config: StorageConfig, calendar: Calendar) = Props(classOf[Storage], config, calendar)

  case class GetAccount(accountId: AccountId)

  case class AccountResponse(accountId: AccountId,
                             state: Option[AccountState],
                             rating: Option[Double],
                             tutorState: Option[TutorState],
                             top: Top,
                             place: Option[Long],
                             lastWeekTop: Top,
                             lastWeekPlace: Option[Long])


  case class GetAndUpdateAccountState(accountId: AccountId, transform: Option[AccountState] ⇒ AccountState)

  case class AccountStateUpdated(accountId: AccountId, newState: AccountState)


  case class GetAndUpdateAccountStateAndRating(accountId: AccountId, transform: (Option[AccountState], Option[Double]) ⇒ (AccountState, Double), userInfo: UserInfo)

  case class AccountStateAndRatingUpdated(accountId: AccountId, newState: AccountState, newRating: Double, newPlace: Long, newTop: Top)


  case class ReplaceTutorState(accountId: AccountId, newTutorState: TutorState)

  case class TutorStateUpdated(accountId: AccountId, newTutorState: TutorState)


  case class ReplaceUserInfo(accountId: AccountId, newUserInfo: UserInfo)

  case class UserInfoUpdated(accountId: AccountId, newUserInfo: UserInfo)

}

class Storage(config: StorageConfig, calendar: Calendar) extends Actor with ShortActorLogging {
  val factory = new MySQLConnectionFactory(config.configuration)
  val pool = new ConnectionPool(factory, config.poolConfiguration)

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
                        successful(AccountResponse(accountId, state, rating, tutorState, top, place, lastWeekTop, lastWeekPlace))
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
            successful(AccountStateUpdated(accountId, newState))
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
                    successful(AccountStateAndRatingUpdated(accountId, newState, newRating, place, top))
                  }
                }
              }
            }
          }
        }
      }

    case ReplaceTutorState(accountId, tutorState) ⇒
      answer(implicit connection ⇒ replaceTutorState(accountId, tutorState))

    case ReplaceUserInfo(accountId, userInfo) ⇒
      answer(implicit connection ⇒ replaceUserInfo(accountId, userInfo))
  }

  def getTop(weekNumber: Int)(implicit connection: Connection): Future[Top] =
    getAll(
      "SELECT ratings.id, ratings.rating, userInfo " +
        "FROM ratings " +
        "LEFT JOIN user_info ON user_info.id=ratings.id " +
        "WHERE ratings.weekNumber=? " +
        "GROUP BY ratings.id " +
        "ORDER BY ratings.rating DESC " +
        "LIMIT 5;",
      Seq(weekNumber),
      rowDataToTopUser
    ) flatMap { users ⇒
      successful(Top(users, weekNumber))
    }

  def getOptionPlace(rating: Option[Double], weekNumber: Int)(implicit connection: Connection): Future[Option[Long]] =
    if (rating.isDefined)
      getPlace(rating.get, weekNumber) flatMap { place ⇒
        successful(Some(place))
      }
    else
      successful(None)

  def getPlace(rating: Double, weekNumber: Int)(implicit connection: Connection): Future[Long] =
    getOne(
      "SELECT COUNT(*) `place` " +
        "FROM ratings " +
        "WHERE weekNumber = ? AND rating > ?",
      Seq(weekNumber, rating),
      rowDataToPlace
    ) flatMap {
      place ⇒ successful(place.get)
    }

  def getRating(accountId: AccountId, weekNumber: Int)(implicit connection: Connection): Future[Option[Double]] =
    getOne(
      "SELECT rating FROM ratings WHERE weekNumber = ? AND id=?;",
      Seq(weekNumber, accountId.toByteArray),
      rowDataToRating
    )

  def replaceRating(accountId: AccountId, weekNumber: Int, newRating: Double, userInfo: UserInfo)(implicit connection: Connection): Future[Unit] =
    write(
      "REPLACE INTO ratings (id,weekNumber,rating) VALUES (?,?,?);",
      Seq(accountId.toByteArray, weekNumber, newRating)
    )

  def replaceAccountState(accountId: AccountId, newState: AccountState)(implicit connection: Connection): Future[Unit] =
    write(
      "REPLACE INTO account_state (id,state) VALUES (?,?);",
      Seq(accountId.toByteArray, newState.toByteArray)
    )

  def getAccountState(accountId: AccountId)(implicit connection: Connection): Future[Option[AccountState]] =
    getOne(
      "SELECT state FROM account_state WHERE id=?;",
      Seq(accountId.toByteArray),
      rowDataToAccountState
    )

  def getTutorState(accountId: AccountId)(implicit connection: Connection): Future[Option[TutorState]] =
    getOne(
      "SELECT state FROM tutor_state WHERE id=?;",
      Seq(accountId.toByteArray),
      rowDataToTutorState
    )

  def replaceTutorState(accountId: AccountId, tutorState: TutorState)(implicit connection: Connection): Future[TutorStateUpdated] =
    write(
      "REPLACE INTO tutor_state (id,state) VALUES (?,?);",
      Seq(accountId.toByteArray, tutorState.toByteArray)
    ) flatMap { _ ⇒
      successful(TutorStateUpdated(accountId, tutorState))
    }

  def replaceUserInfo(accountId: AccountId, userInfo: UserInfo)(implicit connection: Connection): Future[UserInfoUpdated] =
    write(
      "REPLACE INTO user_info (id,userInfo) VALUES (?,?);",
      Seq(accountId.toByteArray, userInfo.toByteArray)
    ) flatMap { _ ⇒
      successful(UserInfoUpdated(accountId, userInfo))
    }

  // utils

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
        if (queryResult.rowsAffected == 1 || queryResult.rowsAffected == 2)
          successful()
        else
          failed(new Exception("Invalid rows affected count " + queryResult + " in query " + query))
      )

  def getOne[T](query: String, values: Seq[Any], parser: RowData ⇒ T)(implicit connection: Connection): Future[Option[T]] =
    get(query, values) flatMap { resultSet ⇒
      if (resultSet.isEmpty)
        successful(None)
      else if (resultSet.size == 1)
        successful(Some(parser(resultSet.head)))
      else
        failed(new Exception("Invalid result rows count = " + resultSet.size + " in query " + query))
    }

  def getAll[T](query: String, values: Seq[Any], parser: RowData ⇒ T)(implicit connection: Connection): Future[IndexedSeq[T]] =
    get(query, values) flatMap { resultSet ⇒
      successful(resultSet.map(parser))
    }

  def get(query: String, values: Seq[Any])(implicit connection: Connection): Future[ResultSet] =
    connection.sendPreparedStatement(query, values) flatMap {
      queryResult ⇒ queryResult.rows match {
        case Some(resultSet) ⇒ successful(resultSet)
        case None ⇒ failed(new Exception("Get none " + queryResult))
      }
    }
}

private object Parsers {
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

  def rowDataToPlace(rowData: RowData) =
    rowData("place").asInstanceOf[Long] + 1

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

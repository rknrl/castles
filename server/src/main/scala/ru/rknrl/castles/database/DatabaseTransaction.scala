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
import protos.{AccountId, AccountState, TutorState, UserInfo}
import ru.rknrl.castles.database.Database._
import ru.rknrl.castles.database.DatabaseTransaction._
import ru.rknrl.castles.matchmaking.Top
import ru.rknrl.log.Logging.ActorLog

import scala.concurrent.duration._

object DatabaseTransaction {

  def props(database: ActorRef, calendar: Calendar) =
    Props(classOf[DatabaseTransaction], database, calendar)

  trait Request {
    val accountId: AccountId
  }

  trait Response {
    val accountId: AccountId
  }

  case class GetAccount(accountId: AccountId) extends Request

  case class AccountResponse(accountId: AccountId,
                             state: Option[AccountState],
                             rating: Option[Double],
                             tutorState: Option[TutorState],
                             top: Top,
                             place: Option[Long],
                             lastWeekPlace: Option[Long],
                             lastWeekTop: Top) extends Response

  case class GetAndUpdateAccountState(accountId: AccountId, transform: Option[AccountState] ⇒ AccountState) extends Request

  case class AccountStateResponse(accountId: AccountId, state: AccountState) extends Response

  case class GetAndUpdateAccountStateAndRating(accountId: AccountId, transform: (Option[AccountState], Option[Double]) ⇒ (AccountState, Double), userInfo: UserInfo) extends Request

  case class AccountStateAndRatingResponse(accountId: AccountId, state: AccountState, rating: Double, place: Long, top: Top) extends Response


  trait Calendar {
    def getCurrentWeek: Int
    def getCurrentMillis: Long
  }

  class RealCalendar extends Calendar {
    val week = 7 * 24 * 60 * 60 * 1000

    def getCurrentMillis: Long = System.currentTimeMillis

    def getCurrentWeek: Int = (System.currentTimeMillis / week).toInt
  }

  class FakeCalendar(week: Int, millis: Long = 3000) extends Calendar {
    def getCurrentMillis: Long = millis

    def getCurrentWeek: Int = week
  }

}

class DatabaseTransaction(database: ActorRef, calendar: Calendar) extends Actor with ActorLog {

  def receive = logged {
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

    case msg ⇒ forward(database, msg)
  }

  val timeout = 10 seconds

  import context.dispatcher

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

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
import ru.rknrl.castles.database.Database._
import ru.rknrl.castles.database.DatabaseTransaction._
import ru.rknrl.castles.matchmaking.Top
import ru.rknrl.dto.{AccountId, AccountStateDTO, TutorStateDTO, UserInfoDTO}
import ru.rknrl.logging.ActorLog

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
                             state: Option[AccountStateDTO],
                             rating: Option[Double],
                             tutorState: Option[TutorStateDTO],
                             top: Top,
                             place: Option[Long],
                             lastWeekPlace: Option[Long],
                             lastWeekTop: Top) extends Response

  case class GetAndUpdateAccountState(accountId: AccountId, transform: Option[AccountStateDTO] ⇒ AccountStateDTO) extends Request

  case class AccountStateResponse(accountId: AccountId, state: AccountStateDTO) extends Response

  case class GetAndUpdateAccountStateAndRating(accountId: AccountId, transform: (Option[AccountStateDTO], Option[Double]) ⇒ (AccountStateDTO, Double), userInfo: UserInfoDTO) extends Request

  case class AccountStateAndRatingResponse(accountId: AccountId, state: AccountStateDTO, rating: Double, place: Long, top: Top) extends Response


  trait Calendar {
    def getCurrentWeek: Int
  }

  class RealCalendar extends Calendar {
    val week = 7 * 24 * 60 * 60 * 1000

    def getCurrentWeek: Int = (System.currentTimeMillis / week).toInt
  }

  class FakeCalendar(week: Int) extends Calendar {
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

  val timeout = 5 seconds

  import context.dispatcher

  def getTop(weekNumber: Int, callback: Top ⇒ Unit): Unit =
    Patterns.ask(database, GetTop(weekNumber), timeout).map {
      case top: Top ⇒ callback(top)
    }

  def getRating(weekNumber: Int, accountId: AccountId, callback: Option[Double] ⇒ Unit): Unit =
    Patterns.ask(database, GetRating(accountId, weekNumber), timeout).map {
      case RatingResponse(accountId, weekNumber, rating) ⇒ callback(rating)
    }

  def getOptionPlace(weekNumber: Int, rating: Option[Double], callback: Option[Long] ⇒ Unit): Unit =
    if (rating.isDefined)
      getPlace(weekNumber, rating.get, place ⇒ callback(Some(place)))
    else
      callback(None)

  def getPlace(weekNumber: Int, rating: Double, callback: Long ⇒ Unit): Unit =
    Patterns.ask(database, GetPlace(rating, weekNumber), timeout).map {
      case PlaceResponse(rating, weekNumber, place) ⇒ callback(place)
    }

  def updateRating(weekNumber: Int, accountId: AccountId, newRating: Double, userInfo: UserInfoDTO, callback: () ⇒ Unit): Unit =
    Patterns.ask(database, UpdateRating(accountId, weekNumber, newRating, userInfo), timeout).map {
      case RatingResponse(accountId, weekNumber, rating) ⇒ callback()
    }

  def getAccountState(accountId: AccountId, callback: Option[AccountStateDTO] ⇒ Unit): Unit =
    Patterns.ask(database, GetAccountState(accountId), timeout).map {
      case Database.AccountStateResponse(accountId, state) ⇒ callback(state)
    }

  def updateAccountState(accountId: AccountId, newState: AccountStateDTO, callback: () ⇒ Unit): Unit =
    Patterns.ask(database, UpdateAccountState(accountId, newState), timeout).map {
      case Database.AccountStateResponse(accountId, state) ⇒ callback()
    }

  def getTutorState(accountId: AccountId, callback: Option[TutorStateDTO] ⇒ Unit): Unit =
    Patterns.ask(database, GetTutorState(accountId), timeout).map {
      case Database.TutorStateResponse(accountId, state) ⇒ callback(state)
    }
}

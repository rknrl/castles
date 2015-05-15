//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.database

import akka.actor.{Actor, ActorRef}
import akka.pattern.Patterns
import ru.rknrl.castles.database.Database._
import ru.rknrl.castles.database.DatabaseTransaction._
import ru.rknrl.dto.{AccountId, AccountStateDTO, TutorStateDTO, UserInfoDTO}
import ru.rknrl.logging.ActorLog

import scala.concurrent.duration._

object DatabaseTransaction {

  trait Request {
    val accountId: AccountId
  }

  trait Response {
    val accountId: AccountId
  }

  trait NoResponse

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

}

class DatabaseTransaction(database: ActorRef) extends Actor with ActorLog {
  def receive = logged {
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
          send(ref, DatabaseTransaction.AccountStateResponse(accountId, newState))
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

    case msg: NoResponse ⇒ send(database, msg)
  }

  val timeout = 5 seconds

  import context.dispatcher

  def getPlace(rating: Double, callback: Long ⇒ Unit): Unit =
    Patterns.ask(database, GetPlace(rating), timeout).map {
      case PlaceResponse(rating, place) ⇒ callback(place)
    }

  def getRating(accountId: AccountId, callback: Option[Double] ⇒ Unit): Unit =
    Patterns.ask(database, GetRating(accountId), timeout).map {
      case RatingResponse(accountId, rating) ⇒ callback(rating)
    }

  def updateRating(accountId: AccountId, newRating: Double, callback: () ⇒ Unit): Unit =
    Patterns.ask(database, UpdateRating(accountId, newRating), timeout).map {
      case RatingResponse(accountId, rating) ⇒ callback()
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

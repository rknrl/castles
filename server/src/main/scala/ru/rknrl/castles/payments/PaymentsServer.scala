package ru.rknrl.castles.payments

import akka.actor.{Actor, ActorRef}
import akka.pattern.Patterns
import ru.rknrl.castles.AccountId
import ru.rknrl.castles.payments.PaymentsCallback.{PaymentResponse, Response}
import ru.rknrl.castles.payments.PaymentsServer._
import ru.rknrl.core.social.SocialConfigs
import spray.http.MediaTypes._
import spray.routing.HttpService

import scala.concurrent.Await
import scala.concurrent.duration._

object PaymentsServer {

  /**
   * Account -> Payments
   */
  case class Register(accountId: AccountId)

  /**
   * Payments -> Account response
   */
  case object Registered

  /**
   * Account -> Payments
   */
  case class Unregister(accountId: AccountId)

  /**
   * Payments -> Account response
   */
  case object Unregistered

  /**
   * Payments -> Account
   */
  case class AddGold(oderId: String, amount: Int)

  /**
   * Account -> Payments response
   */
  case class GoldAdded(orderId: String)

}

class PaymentsServer(config: SocialConfigs) extends Actor with HttpService {

  private var accountIdToAccount = Map[AccountId, ActorRef]()

  private val paymentsCallbacks =
    path("vk_callback") {
      requestUri {
        uri ⇒
          respondWithMediaType(`text/plain`) {
            completePayment(new PaymentsCallbackVk(uri, config.vk.get))
          }
      }
    } ~ path("ok_callback") {
      requestUri {
        uri ⇒
          respondWithMediaType(`application/xml`) {
            completePayment(new PaymentsCallbackOk(uri, config.ok.get))
          }
      }
    } ~ path("mm_callback") {
      requestUri {
        uri ⇒
          respondWithMediaType(`text/plain`) {
            completePayment(new PaymentsCallbackMm(uri, config.mm.get))
          }
      }
    }

  def completePayment(callback: PaymentsCallback) = {
    val response = callback.response

    response match {
      case PaymentResponse(orderId, accountId, price, httpResponse) ⇒
        if (!(accountIdToAccount contains accountId))
          complete(callback.accountNotFoundError)
        else {
          val future = Patterns.ask(accountIdToAccount(accountId), AddGold(orderId, price), 5 seconds)
          val result = Await.result(future, 5 seconds)

          result match {
            case GoldAdded(oId) ⇒
              if (orderId == oId)
                complete(httpResponse)
              else
                complete(callback.databaseError)
            case _ ⇒
              complete(callback.databaseError)
          }
        }

      case Response(httpResponse) ⇒
        complete(httpResponse)

      case _ ⇒
        complete(callback.error)
    }
  }

  def actorRefFactory = context

  def receive = {
    case Register(accountId) ⇒
      accountIdToAccount = accountIdToAccount.updated(accountId, sender)
      sender ! Registered

    case Unregister(accountId) ⇒
      accountIdToAccount = accountIdToAccount - accountId
      sender ! Unregistered

    case _ ⇒ runRoute(paymentsCallbacks)
  }
}

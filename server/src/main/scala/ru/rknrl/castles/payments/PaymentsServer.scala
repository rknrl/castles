//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.payments

import java.net.URLDecoder

import akka.actor.{ActorLogging, ActorRef}
import akka.pattern.Patterns
import org.slf4j.LoggerFactory
import ru.rknrl.StoppingStrategyActor
import ru.rknrl.castles.Config
import ru.rknrl.castles.MatchMaking.AdminSetAccountState
import ru.rknrl.castles.account.state.AccountState
import ru.rknrl.castles.database.Database._
import ru.rknrl.castles.payments.PaymentsCallback.{PaymentResponse, Response}
import ru.rknrl.core.social.SocialConfig
import spray.http.MediaTypes._
import spray.http._
import spray.httpx.marshalling.Marshaller
import spray.routing.HttpService

import scala.concurrent.Await
import scala.concurrent.duration._

class PaymentsServer(config: Config, database: ActorRef, matchmaking: ActorRef) extends StoppingStrategyActor with HttpService with ActorLogging {
  val bugLog = LoggerFactory.getLogger("client")

  implicit val UTF8StringMarshaller =
    Marshaller.of[String](ContentType(`text/plain`, HttpCharsets.`UTF-8`)) { (value, contentType, ctx) ⇒
      ctx.marshalTo(HttpEntity(contentType, value))
    }

  val paymentsCallbacks =
    path("bug") {
      post {
        entity(as[String]) { log =>
          bugLog.info(log)
          complete(StatusCodes.OK)
        }
      }
    } ~ path("vk_callback") {
      post {
        entity(as[String]) { data ⇒
          respondWithMediaType(`application/json`) {
            log.info("vk_callback: " + data)
            val encoded = URLDecoder.decode(data, "UTF-8")
            log.info("encoded: " + encoded)
            val vk = config.social.vk.get
            completePayment(new PaymentsCallbackVk(encoded, vk, config.products), vk)
          }
        }
      }
    } ~ path("ok_callback") {
      requestUri {
        uri ⇒
          respondWithMediaType(`application/xml`) {
            log.info("ok_callback: " + uri)
            val ok = config.social.ok.get
            completePayment(new PaymentsCallbackOk(uri, ok), ok)
          }
      }
    } ~ path("mm_callback") {
      requestUri {
        uri ⇒
          respondWithMediaType(`text/plain`) {
            log.info("mm_callback: " + uri)
            val mm = config.social.mm.get
            completePayment(new PaymentsCallbackMm(uri, mm), mm)
          }
      }
    }

  def completePayment(callback: PaymentsCallback, socialConfig: SocialConfig) = {
    val response = callback.response

    response match {
      case p@PaymentResponse(orderId, accountId, productId, price, httpResponse) ⇒
        log.info("payment callback PaymentResponse " + p)

        if (!(config.products exists (_.id == productId))) {

          log.info("product not found " + productId)
          complete(callback.accountNotFoundError)

        } else {
          val product = config.products.find(_.id == productId).get
          val productInfo = socialConfig.productsInfo.find(_.id == productId).get

          if (productInfo.price != price) {
            log.info("price=" + price + ",expect " + productInfo.price)
            complete(callback.accountNotFoundError)
          } else {
            log.info("AddProduct")

            val future = Patterns.ask(database, GetAccountState(accountId.dto), 5 seconds)
            val result = Await.result(future, 5 seconds)
            result match {
              case AccountStateResponse(_, accountStateDto) ⇒
                val state = AccountState(accountStateDto)
                val newState = state.applyProduct(product, productInfo.count)

                val future = Patterns.ask(database, UpdateAccountState(accountId.dto, newState.dto), 5 seconds)
                val result = Await.result(future, 5 seconds)

                result match {
                  case msg@AccountStateResponse(_, newAccountStateDto) ⇒
                    if (newAccountStateDto.getGold == newState.gold) {
                      matchmaking ! AdminSetAccountState(accountId, newAccountStateDto)
                      complete(httpResponse)
                    } else {
                      log.info("invalid gold=" + newAccountStateDto.getGold + ", but expected " + newState.gold)
                      complete(callback.databaseError)
                    }
                  case invalid ⇒
                    log.info("invalid update result=" + invalid)
                    complete(callback.databaseError)
                }

              case AccountNoExists ⇒
                log.info("account not found")
                complete(callback.accountNotFoundError)

              case invalid ⇒
                log.info("invalid get result=" + invalid)
                complete(callback.databaseError)
            }
          }
        }

      case Response(httpResponse) ⇒
        log.info("payment callback response: " + httpResponse)
        complete(httpResponse)

      case invalid ⇒
        log.info("payment callback invalid response: " + invalid)
        complete(callback.error)
    }
  }

  def actorRefFactory = context

  def receive = runRoute(paymentsCallbacks)
}

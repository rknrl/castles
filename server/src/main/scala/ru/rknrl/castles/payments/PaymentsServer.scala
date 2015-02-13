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
import ru.rknrl.castles.payments.PaymentsCallback.{PaymentResponse, Response}
import ru.rknrl.castles.payments.PaymentsServer._
import ru.rknrl.castles.{AccountId, Config}
import ru.rknrl.core.social.{Product, SocialConfig}
import spray.http.MediaTypes._
import spray.http._
import spray.httpx.marshalling.Marshaller
import spray.routing.HttpService

import scala.concurrent.Await
import scala.concurrent.duration._

object PaymentsServer {

  /** Payments -> Matchmaking */
  case class AddProduct(accountId: AccountId, oderId: String, product: Product, count: Int)

  /** Matchmaking -> Payments */
  case class ProductAdded(orderId: String)

  /** Matchmaking -> Payments */
  case object AccountNotFound

  /** Matchmaking -> Payments */
  case object DatabaseError

}

class PaymentsServer(config: Config, matchmaking: ActorRef) extends StoppingStrategyActor with HttpService with ActorLogging {
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
            val future = Patterns.ask(matchmaking, AddProduct(accountId, orderId, product, productInfo.count), 5 seconds)
            val result = Await.result(future, 5 seconds)

            result match {
              case ProductAdded(oId) ⇒
                if (orderId == oId) {
                  log.info("ProductAdded complete: " + httpResponse)
                  complete(httpResponse)
                } else {
                  log.info("ProductAdded orderId=" + oId + ", expected " + orderId)
                  complete(callback.databaseError)
                }

              case AccountNotFound ⇒
                log.info("account not found " + accountId)
                complete(callback.accountNotFoundError)

              case invalid ⇒
                log.info("Add to database invalid result " + invalid)
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

package ru.rknrl.base.payments

import java.net.{URLDecoder}

import akka.actor.{ActorLogging, ActorRef}
import akka.pattern.Patterns
import org.slf4j.LoggerFactory
import ru.rknrl.StoppingStrategyActor
import ru.rknrl.base.AccountId
import ru.rknrl.base.payments.PaymentsCallback.{PaymentResponse, Response}
import ru.rknrl.base.payments.PaymentsServer._
import ru.rknrl.castles.Config
import ru.rknrl.core.social.{Product, SocialConfig}
import spray.http.MediaTypes._
import spray.http._
import spray.httpx.marshalling.Marshaller
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
  case class AddProduct(oderId: String, product: Product, count: Int)

  /**
   * Account -> Payments response
   */
  case class ProductAdded(orderId: String)

}

class PaymentsServer(config: Config) extends StoppingStrategyActor with HttpService with ActorLogging {
  private val bugLog = LoggerFactory.getLogger("client")

  private var accountIdToAccount = Map[AccountId, ActorRef]()

  implicit val UTF8StringMarshaller =
    Marshaller.of[String](ContentType(`text/plain`, HttpCharsets.`UTF-8`)) { (value, contentType, ctx) ⇒
      ctx.marshalTo(HttpEntity(contentType, value))
    }

  private val paymentsCallbacks =
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

        if (!(accountIdToAccount contains accountId)) {

          log.info("account not found " + accountId)
          complete(callback.accountNotFoundError)

        } else if (!(config.products exists (_.id == productId))) {

          log.info("product not found " + productId)
          complete(callback.accountNotFoundError)

        } else {
          val product = config.products.find(_.id == productId).get
          val productInfo = socialConfig.productsInfo.find(_.id == productId).get

          if (productInfo.price != price) {
            log.info("price=" + price + ",expect " + productInfo.price)
            complete(callback.accountNotFoundError)
          } else {
            val future = Patterns.ask(accountIdToAccount(accountId), AddProduct(orderId, product, productInfo.count), 5 seconds)
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

  def receive = runRoute(paymentsCallbacks) orElse {
    case Register(accountId) ⇒
      accountIdToAccount = accountIdToAccount.updated(accountId, sender)
      sender ! Registered

    case Unregister(accountId) ⇒
      accountIdToAccount = accountIdToAccount - accountId
      sender ! Unregistered
  }
}

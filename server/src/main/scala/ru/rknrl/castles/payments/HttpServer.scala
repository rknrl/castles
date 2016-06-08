//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.payments

import java.net.URLDecoder

import akka.actor.{Actor, ActorRef, Props}
import akka.io.IO
import akka.pattern.Patterns
import ru.rknrl.castles.Config
import ru.rknrl.castles.account.AccountState
import ru.rknrl.castles.storage.Storage.{AccountStateResponse, GetAndUpdateAccountState}
import ru.rknrl.castles.payments.PaymentsCallback.{PaymentResponse, Response}
import ru.rknrl.core.social.SocialConfig
import ru.rknrl.logging.Bugs.Bug
import ru.rknrl.logging.ShortActorLogging
import spray.can.Http
import spray.http.MediaTypes._
import spray.http._
import spray.httpx.marshalling.Marshaller
import spray.routing.HttpService

import scala.concurrent.duration._

object HttpServer {
  def props(config: Config,
            storage: ActorRef,
            matchmaking: ActorRef,
            bugs: ActorRef) =
    Props(classOf[HttpServer], config, storage, matchmaking, bugs)
}

class HttpServer(config: Config,
                 storage: ActorRef,
                 matchmaking: ActorRef,
                 bugs: ActorRef) extends Actor with ShortActorLogging with HttpService {

  implicit val system = context.system

  IO(Http) ! Http.Bind(self, config.host, config.httpPort)

  val crossdomain =
    """<?xml version="1.0"?>
      |<!DOCTYPE cross-domain-policy SYSTEM "/xml/dtds/cross-domain-policy.dtd">
      |<cross-domain-policy>
      |<site-control permitted-cross-domain-policies="master-only"/>
      |<allow-access-from domain="*" to-ports="*"/>
      |</cross-domain-policy>""".stripMargin

  implicit val UTF8StringMarshaller =
    Marshaller.of[String](ContentType(`text/plain`, HttpCharsets.`UTF-8`)) { (value, contentType, ctx) ⇒
      ctx.marshalTo(HttpEntity(contentType, value))
    }

  val paymentsCallbacks =
    path("bug") {
      post {
        entity(as[String]) { clientLog =>
          bugs ! Bug(clientLog)
          complete(StatusCodes.OK)
        }
      }
    } ~ path("crossdomain.xml") {
      respondWithMediaType(`application/xml`) {
        complete(crossdomain)
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

          log.error("product not found " + productId)
          complete(callback.accountNotFoundError)

        } else {
          val product = config.products.find(_.id == productId).get
          val productInfo = socialConfig.productsInfo.find(_.id == productId).get

          if (productInfo.price != price) {
            log.info("price=" + price + ",expect " + productInfo.price)
            complete(callback.accountNotFoundError)
          } else {
            log.info("ApplyProduct")

            val transform = (stateOption: Option[protos.AccountState]) ⇒ {
              val state = stateOption.getOrElse(config.account.initState)
              AccountState.applyProduct(state, product, productInfo.count)
            }

            import context.dispatcher

            val result = Patterns.ask(storage, GetAndUpdateAccountState(accountId, transform), 5 seconds) map {
              case AccountStateResponse(accountId, accountStateDto) ⇒
                send(matchmaking, AccountStateResponse(accountId, accountStateDto))
                httpResponse
              case invalid ⇒
                log.info("invalid update result=" + invalid)
                callback.databaseError
            }

            complete(result)
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

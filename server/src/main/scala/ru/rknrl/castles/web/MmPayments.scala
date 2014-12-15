package ru.rknrl.castles.web

import akka.actor.Actor
import spray.http.HttpResponse
import spray.http.MediaTypes._
import spray.routing.HttpService

/**
 * http://api.mail.ru/docs/guides/billing/
 */
class MmPayments extends Actor with HttpService {

  /**
   * Если приложение не смогло найти пользователя для оказания услуги
   */
  val USER_NOT_FOUND = 701

  /**
   * Если услуга с данный идентификатором не существуем в вашем приложении
   */
  val SERVICE_NOT_FOUND = 702

  /**
   * Если данная услуга для данного пользователя не могла быть оказана за указанную цену
   */
  val INCORRECT_PRICE = 703

  /**
   * Другая ошибка
   */
  val OTHER_ERROR = 700

  /**
   * Услуга на данный момент не оказана, но может быть оказана позднее
   */
  val STATUS_TEMP_FAIL = 0

  /**
   * Услуга оказана успешно
   */
  val STATUS_SUCCESS = 1

  /**
   * Услуга не может быть оказана
   */
  val STATUS_FAIL = 2

  def actorRefFactory = context

  val myRoute =
    path("") {
      requestUri { uri ⇒
        try {
          val params = new UriParams(uri)

          val appId = params.getParam("app_id") // int	идентификатор вашего приложения

          val transactionId = params.getParam("transaction_id").toInt // int	идентификатор денежной транзакции

          val serviceId = params.getParam("service_id").toInt // unsigned int	идентификатор услуги, который был передан при вызове функции API по приему платежа

          val uid = params.getParam("uid") // string	идентификатор пользователя, который оплатил услугу

          val sig = params.getParam("sig") // string	подпись запроса, рассчитывается по аналогии с подписью запроса любого вызова API по защищенной схеме «сервер-сервер»

          val mailikiPrice = params.getParam("mailiki_price").toInt //	int	номинал платежа в мэйликах, который был указан при вызове платежного окна

          val otherPrice = params.getParam("other_price").toInt //	int	номинал платежа в копейках (для поддержки совместимости с старым протоколом)

          val profit = params.getParam("profit").toInt //	int	сумма в копейках, которую вы получите от Платформы (ваша прибыль)

          val debug = params.hasParam("debug") //	bool	флаг, определяющий режим отладки; если debug=1, то приложение должно учитывать, что это тестовый вызов. При реальных платежах параметр debug отсутствует

          respondWithMediaType(`text/plain`) {
            complete(HttpResponse(entity = successResponse()))
          }
        } catch {
          case _: Throwable ⇒
            respondWithMediaType(`text/plain`) {
              complete(HttpResponse(entity = errorResponse(STATUS_FAIL, OTHER_ERROR)))
            }
        }
      }
    }

  def successResponse() = s"""{"status":"1"}"""

  def errorResponse(status: Int, errorCode: Int) = s"""{"status":"$status","error_code":"$errorCode"}"""

  def receive = runRoute(myRoute)
}

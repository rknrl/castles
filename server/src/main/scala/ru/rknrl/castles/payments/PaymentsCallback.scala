package ru.rknrl.castles.payments

import org.slf4j.LoggerFactory
import ru.rknrl.castles.AccountId
import ru.rknrl.castles.payments.PaymentsCallback.{Response, ResponseBase}
import spray.http.HttpResponse

object PaymentsCallback {

  trait ResponseBase

  /**
   * Платеж
   * @param orderId       идентификатор заказа в системе платежей соц. сети
   * @param accountId     идентификатор получателя заказа (он же и сделал заказ)
   * @param price         стоимость в валюте соц. cети
   * @param httpResponse  ответ для соц сети
   */
  case class PaymentResponse(orderId: String, accountId: AccountId, productId: Int, price: Int, httpResponse: HttpResponse) extends ResponseBase

  /**
   * Не платеж
   */
  case class Response(httpResponse: HttpResponse) extends ResponseBase

}

/**
 * Обработчик платежных сообщений от соц. сети
 */
trait PaymentsCallback {
  val log = LoggerFactory.getLogger("payment")

  def response: ResponseBase

  /**
   * У нас что-то сломалось
   * НЕ ожидаем перезапроса от соц. сети
   */
  def error: HttpResponse

  /**
   * Временная ошибка базы данных
   * Ожидаем перезапроса от соц. сети
   */
  def databaseError: HttpResponse

  /**
   * Пользователя не существует
   * НЕ ожидаем перезапроса от соц. сети
   */
  def accountNotFoundError: HttpResponse

  /**
   * Товара не существует
   * НЕ ожидаем перезапроса от соц. сети
   */
  def itemNotFoundError: HttpResponse

  /**
   * Цена в запросе не соответствует цене на сервере
   * НЕ ожидаем перезапроса от соц. сети
   */
  def invalidPriceError: HttpResponse

  protected def response(string: String) = Response(HttpResponse(entity = string))
}

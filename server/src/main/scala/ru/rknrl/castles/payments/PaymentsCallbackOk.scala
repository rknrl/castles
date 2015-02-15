//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.payments

import akka.util.Crypt
import ru.rknrl.castles.AccountId
import ru.rknrl.castles.payments.PaymentsCallback.{PaymentResponse, Response}
import ru.rknrl.core.social.SocialConfig
import ru.rknrl.dto.CommonDTO.AccountType
import spray.http.HttpHeaders.RawHeader
import spray.http._

/**
 * http://apiok.ru/wiki/pages/viewpage.action?pageId=46137373#APIДокументация(Русский)-Платёжка
 */
class PaymentsCallbackOk(uri: Uri, config: SocialConfig) extends PaymentsCallback {

  override def error = errorResponse(OkErrorCode.SYSTEM)

  override def databaseError = errorResponse(OkErrorCode.SERVICE)

  override def accountNotFoundError = errorResponse(OkErrorCode.CALLBACK_INVALID_PAYMENT)

  override def itemNotFoundError = errorResponse(OkErrorCode.CALLBACK_INVALID_PAYMENT)

  override def invalidPriceError = errorResponse(OkErrorCode.CALLBACK_INVALID_PAYMENT)

  override def response =
    try {
      val params = UriParams.parseGet(uri)

      // (String) Идентификатор пользователя
      val uid = params.getParam("uid")

      // (DateTime) Время транзакции
      val transactionTime = params.getParam("transaction_time")

      // (String) Уникальный идентификатор транзакции
      val transactionId = params.getParam("transaction_id")

      // (String) Код продукта
      val productCode = params.getParam("product_code")

      // (String) Код выбранного варианта продукта
      val productOption = params.getParam("product_option")

      // Optional (Int) Общая сумма в виртуальной валюте портала
      val amount = params.getParam("amount").toInt

      // Optional (String) Валюта платежа (за исключением платежей в «ok»)
      val currency = params.getParam("currency")

      // Optional (String) Система оплаты в случае прямых платежей в валюте RUR
      val paymentSystem = params.getParam("payment_system")

      // Optional (String) JSON-кодированные пары ключей/значений, содержащие дополнительные параметры транзакции,
      // которые передает приложение в методе FAPI.UI.showPayment.
      val extraAttributes = params.getParam("extra_attributes")

      val authKey = params.getParam("auth_key")
      val sessionKey = params.getParam("session_key")

      if (authKey.toUpperCase != Crypt.md5(uid + sessionKey + config.appSecret))
        Response(errorResponse(OkErrorCode.PARAM_SIGNATURE))
      else
        PaymentResponse(
          orderId = transactionId,
          accountId = new AccountId(AccountType.ODNOKLASSNIKI, uid),
          productId = productCode.toInt,
          price = amount,
          httpResponse = HttpResponse(entity = successResponse())
        )
    } catch {
      case e: Throwable ⇒
        log.error("error request parsing ", e)
        Response(errorResponse(OkErrorCode.CALLBACK_INVALID_PAYMENT))
    }

  private def successResponse() =
    """
      |<?xml version="1.0" encoding="UTF-8"?>
      |<callbacks_payment_response xmlns="http://api.forticom.com/1.0/">
      |true
      |</callbacks_payment_response>
    """.stripMargin

  private def errorResponse(error: OkErrorCode) =
    HttpResponse(
      headers = List(RawHeader("Invocation-error", error.code.toString)),
      entity = error.toString
    )
}

class OkErrorCode private(val code: Int, val description: String) {
  override def toString =
    s"""
       |<?xml version="1.0" encoding="UTF-8"?>
       |<ns2:error_response xmlns:ns2='http://api.forticom.com/1.0/'>
       |   <error_code>$code</error_code>
                              |   <error_msg>$description</error_msg>
                                                           |</ns2:error_response>
    """.stripMargin
}

object OkErrorCode {
  val UNKNOWN = new OkErrorCode(1, "Неизвестная ошибка")

  val SERVICE = new OkErrorCode(2, "Сервис временно недоступен")

  val CALLBACK_INVALID_PAYMENT = new OkErrorCode(1001, "Платеж неверный и не может быть обработан")

  val SYSTEM = new OkErrorCode(9999, "Критический системный сбой, который невозможно устранить")

  val PARAM_SIGNATURE = new OkErrorCode(104, "Неверная подпись")
}

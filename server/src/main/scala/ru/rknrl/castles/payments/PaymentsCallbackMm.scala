//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.payments

import akka.util.Crypt
import protos.{AccountId, AccountType}
import ru.rknrl.Assertion
import ru.rknrl.castles.payments.PaymentsCallback.PaymentResponse
import ru.rknrl.core.social.SocialConfig
import spray.http.{HttpResponse, Uri}

object MmErrorCode {
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
}

object MmStatus {
  /**
    * Услуга на данный момент не оказана, но может быть оказана позднее
    */
  val TEMP_FAIL = 0

  /**
    * Услуга оказана успешно
    */
  val SUCCESS = 1

  /**
    * Услуга не может быть оказана
    */
  val FAIL = 2
}

/**
  * http://api.mail.ru/docs/guides/billing/
  */
class PaymentsCallbackMm(uri: Uri, config: SocialConfig) extends PaymentsCallback {

  override def error = HttpResponse(entity = errorResponse(MmStatus.FAIL, MmErrorCode.OTHER_ERROR))

  override def databaseError = HttpResponse(entity = errorResponse(MmStatus.TEMP_FAIL, MmErrorCode.OTHER_ERROR))

  override def accountNotFoundError = HttpResponse(entity = errorResponse(MmStatus.FAIL, MmErrorCode.USER_NOT_FOUND))

  override def itemNotFoundError = HttpResponse(entity = errorResponse(MmStatus.FAIL, MmErrorCode.SERVICE_NOT_FOUND))

  override def invalidPriceError = HttpResponse(entity = errorResponse(MmStatus.FAIL, MmErrorCode.INCORRECT_PRICE))

  override def response =
    try {
      val params = UriParams.parseGet(uri)

      // (Int) идентификатор вашего приложения
      val appId = params.getParam("app_id")

      Assertion.check(appId == config.appId)

      // (Int) идентификатор денежной транзакции
      val transactionId = params.getParam("transaction_id")

      // (Uint) идентификатор услуги, который был передан при вызове функции API по приему платежа
      val serviceId = params.getParam("service_id").toInt

      // (String) идентификатор пользователя, который оплатил услугу
      val uid = params.getParam("uid")

      // (String) подпись запроса, рассчитывается по аналогии с подписью запроса любого вызова API по защищенной схеме «сервер-сервер»
      val sig = params.getParam("sig")

      // (Int) номинал платежа в мэйликах, который был указан при вызове платежного окна
      val mailikiPrice = params.getParam("mailiki_price").toInt

      // (Int) номинал платежа в копейках (для поддержки совместимости с старым протоколом)
      val otherPrice = params.getParam("other_price").toInt

      // (Int) сумма в копейках, которую вы получите от Платформы (ваша прибыль)
      val profit = params.getParam("profit").toInt

      // (Bool) флаг, определяющий режим отладки; если debug=1, то приложение должно учитывать, что это тестовый вызов.
      // При реальных платежах параметр debug отсутствует
      val debug = params.hasParam("debug")

      if (sig != Crypt.md5(params.concat + config.appSecret))
        super.response(errorResponse(MmStatus.FAIL, MmErrorCode.OTHER_ERROR))
      else
        PaymentResponse(
          orderId = transactionId,
          accountId = AccountId(AccountType.MOIMIR, uid),
          productId = serviceId,
          price = mailikiPrice,
          httpResponse = HttpResponse(entity = successResponse())
        )
    } catch {
      case e: Throwable ⇒
        log.error("payments", e)
        super.response(errorResponse(MmStatus.FAIL, MmErrorCode.OTHER_ERROR))
    }

  private def successResponse() = s"""{"status":"1"}"""

  private def errorResponse(status: Int, errorCode: Int) = s"""{"status":"$status","error_code":"$errorCode"}"""
}

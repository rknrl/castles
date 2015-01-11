package ru.rknrl.castles.payments

import akka.util.Crypt
import ru.rknrl.castles.AccountId
import ru.rknrl.castles.payments.PaymentsCallback.PaymentResponse
import ru.rknrl.core.social.SocialConfig
import ru.rknrl.dto.CommonDTO.AccountType
import spray.http.{HttpResponse, Uri}

object VkNotificationType {
  /**
   * получение информации о товаре
   */
  val GET_ITEM = "get_item"

  /**
   * изменение статуса заказа
   */
  val ORDER_STATUS_CHANGE = "order_status_change"
}

object VkStatus {
  /**
   * заказ готов к оплате.
   * Необходимо оформить заказ пользователю внутри приложения.
   * В случае ответа об успехе платёжная система зачислит голоса на счёт приложения.
   * Если в ответ будет получено сообщение об ошибке, заказ отменяется.
   */
  val CHARGABLE = "chargeable"
}

object VkLang {
  val ru_RU = "ru_RU"
  val uk_UA = "uk_UA"
  val be_BY = "be_BY"
  val en_US = "en_US"
}

/**
 * 15 dec 2014
 * http://vk.com/dev/payments_callbacks
 */
class PaymentsCallbackVk(uri: Uri, config: SocialConfig) extends PaymentsCallback {

  override def error = HttpResponse(entity = VkPaymentsError.COMMON(critical = true).toString)

  override def databaseError = HttpResponse(entity = VkPaymentsError.DATABASE_ERROR.toString)

  override def accountNotFoundError = HttpResponse(entity = VkPaymentsError.ACCOUNT_NOT_FOUND.toString)

  override def itemNotFoundError = HttpResponse(entity = VkPaymentsError.ITEM_NOT_FOUND.toString)

  override def invalidPriceError = HttpResponse(entity = VkPaymentsError.INVALID_REQUEST.toString)

  override def response =
    try {
      val params = new UriParams(uri)

      // (String) тип уведомления
      val notificationType = params.getParam("notification_type")

      // (Int) идентификатор приложения
      val appId = params.getParam("app_id")

      assert(appId == config.appId)

      // (Int) идентификатор пользователя, сделавшего заказ
      val userId = params.getParam("user_id")

      val accountId = new AccountId(AccountType.VKONTAKTE, userId)

      // (Int) идентификатор получателя заказа (в данный момент совпадает с user_id, но в будущем может отличаться)
      val receiverId = params.getParam("receiver_id")

      assert(userId == receiverId)

      // (Int) идентификатор заказа
      val orderId = params.getParam("order_id")

      // (String) наименование товара, переданное диалоговому окну покупки (см. Параметры диалогового окна платежей)
      val item = params.getParam("item")

      // (String) подпись уведомления
      val sig = params.getParam("sig")

      if (sig.toUpperCase != Crypt.md5(params.concat + config.appSecret))
        response(VkPaymentsError.INVALID_SIG.toString)
      else
        notificationType match {
          /**
           * http://vk.com/dev/payments_status
           */
          case VkNotificationType.ORDER_STATUS_CHANGE ⇒
            // (Int) дата создания заказа (в формате unix timestamp)
            val date = params.getParam("date")

            // (String) новый статус заказа
            val status = params.getParam("status")

            assert(status == VkStatus.CHARGABLE)

            // (String) идентификатор товара в приложении
            val itemId = params.getParam("item_id")

            // (String) идентификатор товара в приложении
            val itemTitle = params.getParam("item_title")

            // (String) изображение товара
            val itemPhotoUrl = params.getParam("item_photo_url")

            // (String) стоимость товара
            val itemPrice = params.getParam("item_price").toInt

            PaymentResponse(
              orderId = orderId,
              accountId = accountId,
              productId = itemId.toInt,
              price = itemPrice,
              httpResponse = HttpResponse(entity = successResponse(orderId, appOrderId = None))
            )

          /**
           * http://vk.com/dev/payments_getitem
           */
          case VkNotificationType.GET_ITEM ⇒
            // (String) язык пользователя в формате язык_страна.
            val lang = params.getParam("lang")

            response(
              itemResponse(
                title = "Звезды",
                photoUrl = Some("http://звезыикон"),
                price = 1,
                itemId = Some("1"),
                expiration = None
              )
            )

          case _ ⇒
            response(VkPaymentsError.INVALID_REQUEST.toString)
        }
    } catch {
      case _: Throwable ⇒
        response(VkPaymentsError.INVALID_REQUEST.toString)
    }

  /**
   * @param orderId     идентификатор заказа в системе платежей ВКонтакте
   * @param appOrderId  (Optional) идентификатор заказа в приложении. Должен быть уникальным для каждого заказа.
   */
  private def successResponse(orderId: String, appOrderId: Option[Int]) = {
    val appOrderIdStr = if (appOrderId.isDefined) s""", "app_order_id":${appOrderId.get}""" else ""

    s"""
    |{
    |  "response": {
    |    "order_id":$orderId
    |    $appOrderIdStr
    |  }
    |}""".stripMargin
  }

  /**
   * @param title       название товара, до 48 символов
   * @param photoUrl    (Optional) URL изображения товара на сервере разработчика. Рекомендуемый размер изображения – 75х75px.
   * @param price       стоимость товара в голосах
   * @param itemId      (Optional) идентификатор товара в приложении
   * @param expiration  (Optional) разрешает кэширование товара на {expiration} секунд.
   *                    Допустимый диапазон от 600 до 604800 секунд.
   *                    Внимание! При отсутствии параметра возможно кэширование товара на 3600 секунд при
   *                    большом количестве подряд одинаковых ответов.
   *                    Для отмены кэширования необходимо передать 0 в качестве значения параметра.
   */
  private def itemResponse(title: String, photoUrl: Option[String], price: Int, itemId: Option[String], expiration: Option[Int]) = {
    val photoUrlStr = if (photoUrl.isDefined) s""""photo_url":"${photoUrl.get}",""" else ""

    val itemIdStr = if (itemId.isDefined) s""""item_id":"${itemId.get}",""" else ""

    val expirationStr = if (expiration.isDefined) s""""item_id":"${expiration.get}"""" else ""

    s"""
     |{
     |  "response": {
     |    "title":"$title",
     |    $photoUrlStr
     |    $itemIdStr
     |    "price": $price,
     |    $expirationStr
     |  }
     |}""".stripMargin
  }
}

/**
 * @param errorCode   числовой код ошибки
 * @param description описание ошибки в текстовом виде для чтения человеком,
 *                    обязательно для ошибок задаваемых продавцом
 * @param critical    true, если повторение уведомления с такими же параметрами приведёт к такой же ошибке
 *                    (например, указанного товара не существует).
 *                    Уведомление не будет отправляться повторно, пользователь получит ошибку.
 *
 *                    false, если ошибка временная, и уведомление может быть обработано позже
 *                    (например, временная ошибка записи в базу данных).
 *                    Уведомление будет отправлено через некоторое время, пользователь будет ждать ответа.
 */
private class VkPaymentsError private(val errorCode: Int,
                                      val description: String,
                                      val critical: Boolean) {

  private def criticalToString = if (critical) "true" else "false"

  override def toString =
    s"""
      |{
      |  "error": {
      |    "error_code": $errorCode,
      |    "error_msg": $description,
      |    "critical": $criticalToString
      |  }
      |}
    """.stripMargin
}

/**
 * http://vk.com/dev/payments_errors
 */
private object VkPaymentsError {
  def COMMON(critical: Boolean) = new VkPaymentsError(errorCode = 1, description = "общая ошибка", critical = critical)

  val DATABASE_ERROR = new VkPaymentsError(errorCode = 2, description = "временная ошибка базы данных", critical = false)

  val INVALID_SIG = new VkPaymentsError(errorCode = 10, description = "несовпадение вычисленной и переданной подписи", critical = true)

  val INVALID_REQUEST = new VkPaymentsError(errorCode = 11, description = "параметры запроса не соответствуют спецификации", critical = true)

  val ITEM_NOT_FOUND = new VkPaymentsError(errorCode = 20, description = "товара не существует", critical = true)

  val ITEM_NOT_AVAILABLE = new VkPaymentsError(errorCode = 21, description = "товара нет в наличии", critical = true)

  val ACCOUNT_NOT_FOUND = new VkPaymentsError(errorCode = 22, description = "пользователя не существует", critical = true)

  // ошибки с номерами 100-999 задаются приложением,
  // при возврате таких ошибок обязательно должно присутствовать текстовое описание ошибки.
}

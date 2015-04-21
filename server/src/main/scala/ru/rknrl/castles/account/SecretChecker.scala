//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import akka.actor.Actor
import akka.util.Crypt
import org.apache.commons.codec.binary.Base64
import org.json.JSONObject
import ru.rknrl.castles.Config
import ru.rknrl.castles.account.SecretChecker.{SecretChecked, _}
import ru.rknrl.core.social.SocialConfig
import ru.rknrl.dto.AccountType._
import ru.rknrl.dto.AuthenticateDTO

object SecretChecker {

  case class SecretChecked(valid: Boolean)

  /**
   * auth_key  http://vk.com/dev/apps_init
   */
  def checkSecretVk(secret: String, uid: String, config: SocialConfig) =
    secret.toUpperCase == Crypt.md5(config.appId + '_' + uid + '_' + config.appSecret)

  /**
   * auth_sig   http://apiok.ru/wiki/pages/viewpage.action?pageId=42476523
   */
  def checkSecretOk(secret: String, sessionKey: String, uid: String, config: SocialConfig) =
    secret.toUpperCase == Crypt.md5(uid + sessionKey + config.appSecret)

  /**
   * sig      http://api.mail.ru/docs/guides/restapi/#session
   * http://api.mail.ru/docs/guides/social-apps/
   */
  def checkSecretMm(secret: String, params: String, config: SocialConfig) =
    secret.toUpperCase == Crypt.md5(params + config.appSecret)

  /**
   * signed_request  https://developers.facebook.com/docs/facebook-login/using-login-with-games
   */
  def checkSecretFb(secret: String, uid: String, config: SocialConfig) = {
    val split = secret.split('.')
    val encodedSig = split(0)
    val payload = split(1)
    val data = decode(payload)
    val jsonData = new JSONObject(data)
    encodedSig == signAndEncode(config.appSecret, payload) && jsonData.getString("user_id") == uid
  }

  private val charset = "UTF-8"

  private def signAndEncode(key: String, payload: String) = {
    val algorithm = "HmacSHA256"
    val mac = Mac.getInstance(algorithm)
    mac.init(new SecretKeySpec(key.getBytes(charset), algorithm))
    encode(mac.doFinal(payload.getBytes(charset)))
  }

  private def decode(s: String): String = new String(new Base64(true).decode(s), charset)

  //the true param makes it url-safe, as per signed request spec
  private def encode(bs: Array[Byte]): String = new String(Base64.encodeBase64URLSafe(bs), charset)
}

class SecretChecker(config: Config) extends Actor {
  def receive = {
    case authenticate: AuthenticateDTO ⇒
      sender ! SecretChecked(checkSecret(authenticate))
  }

  def checkSecret(authenticate: AuthenticateDTO) =
    authenticate.userInfo.accountId.accountType match {
      case DEV ⇒
        config.isDev

      case VKONTAKTE ⇒
        if (authenticate.secret.accessToken.isDefined)
          true // todo: check access token
        else
          checkSecretVk(authenticate.secret.body, authenticate.userInfo.accountId.id, config.social.vk.get)

      case ODNOKLASSNIKI ⇒
        if (authenticate.secret.accessToken.isDefined)
          true // todo: check access token
        else
          checkSecretOk(authenticate.secret.body, authenticate.secret.getParams, authenticate.userInfo.accountId.id, config.social.ok.get)

      case MOIMIR ⇒
        if (authenticate.secret.accessToken.isDefined)
          true // todo: check access token
        else
          checkSecretMm(authenticate.secret.body, authenticate.secret.getParams, config.social.mm.get)

      case FACEBOOK ⇒
        checkSecretFb(authenticate.secret.body, authenticate.userInfo.accountId.id, config.social.fb.get)

      case DEVICE_ID ⇒
        true

      case _ ⇒ false
    }
}
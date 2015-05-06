//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles

import ru.rknrl.castles.account.AccountConfig
import ru.rknrl.castles.database.DbConfiguration
import ru.rknrl.castles.game._
import ru.rknrl.core.Graphite.GraphiteConfig
import ru.rknrl.core.social.{Product, SocialConfigs}
import ru.rknrl.dto._

class Config(val host: String,
             val staticHost: String,
             val gamePort: Int,
             val policyPort: Int,
             val adminPort: Int,
             val httpPort: Int,
             val adminLogin: String,
             val adminPassword: String,
             val isDev: Boolean,
             val mapsDir: String,
             val clientBugsDir: String,
             val db: DbConfiguration,
             val graphite: GraphiteConfig,
             val products: List[Product],
             val social: SocialConfigs,
             val account: AccountConfig,
             val game: GameConfig) {

  private def checkProductInfoConfig() =
    for (p ← products)
      for (social ← List(social.vk, social.ok, social.mm))
        if (social.isDefined)
          if (!social.get.productsInfo.exists(_.id == p.id)) throw new Error("can't find product info in config " + p.id)

  checkProductInfoConfig()

  private def socialByAccountType(platformType: PlatformType, accountType: AccountType) =
    platformType match {
      case PlatformType.CANVAS ⇒
        accountType match {
          case AccountType.DEV ⇒ social.vk
          case AccountType.VKONTAKTE ⇒ social.vk
          case AccountType.ODNOKLASSNIKI ⇒ social.ok
          case AccountType.MOIMIR ⇒ social.mm
          case AccountType.FACEBOOK ⇒ social.fb
          case _ ⇒ throw new IllegalArgumentException("unknown accountType " + accountType)
        }
      case PlatformType.IOS ⇒
        accountType match {
          case AccountType.DEV ⇒ social.vk // todo
          case AccountType.DEVICE_ID ⇒ social.vk // todo
          case AccountType.VKONTAKTE ⇒ social.vk // todo
          case AccountType.ODNOKLASSNIKI ⇒ social.vk // todo
          case AccountType.MOIMIR ⇒ social.vk // todo
          case AccountType.FACEBOOK ⇒ social.vk // todo
          case _ ⇒ throw new IllegalArgumentException("unknown accountType " + accountType)
        }
      case PlatformType.ANDROID ⇒
        accountType match {
          case AccountType.DEV ⇒ social.vk // todo
          case AccountType.DEVICE_ID ⇒ social.vk // todo
          case AccountType.VKONTAKTE ⇒ social.vk // todo
          case AccountType.ODNOKLASSNIKI ⇒ social.vk // todo
          case AccountType.MOIMIR ⇒ social.vk // todo
          case AccountType.FACEBOOK ⇒ social.vk // todo
          case _ ⇒ throw new IllegalArgumentException("unknown accountType " + accountType)
        }
    }

  def productsDto(platformType: PlatformType, accountType: AccountType) =
    for (p ← products;
         productInfo = socialByAccountType(platformType, accountType).get.productsInfo.find(_.id == p.id).get)
      yield ProductDTO(
        id = p.id,
        title = p.title,
        description = p.description,
        photoUrl = p.photoUrl,
        count = productInfo.count,
        price = productInfo.price,
        currency = productInfo.currency
      )

  def botUserInfo(accountId: AccountId, number: Int) =
    number match {
      case 0 ⇒
        UserInfoDTO(
          accountId,
          Some("Sasha"),
          Some("Serova"),
          Some("http://" + staticHost + "/avatars/Sasha96.png"),
          Some("http://" + staticHost + "/avatars/Sasha256.png")
        )
      case 1 ⇒
        UserInfoDTO(
          accountId,
          Some("Napoleon"),
          Some("1769"),
          Some("http://" + staticHost + "/avatars/Napoleon96.png"),
          Some("http://" + staticHost + "/avatars/Napoleon256.png")
        )
      case 2 ⇒
        UserInfoDTO(
          accountId,
          Some("Виктория"),
          Some("Викторовна"),
          Some("http://" + staticHost + "/avatars/Babka96.png"),
          Some("http://" + staticHost + "/avatars/Babka256.png")
        )
    }
}
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
import ru.rknrl.castles.payments.BugsConfig
import ru.rknrl.core.social.{Product, SocialConfigs}
import ru.rknrl.dto.AuthDTO.ProductDTO
import ru.rknrl.dto.CommonDTO._

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
             val db: DbConfiguration,
             val products: List[Product],
             val social: SocialConfigs,
             val account: AccountConfig,
             val game: GameConfig,
             val bugs: BugsConfig) {

  private def checkProductInfoConfig() =
    for (p ← products)
      for (social ← List(social.vk, social.ok, social.mm))
        if (social.isDefined)
          if (!social.get.productsInfo.exists(_.id == p.id)) throw new Exception("can't find product info in config " + p.id)

  checkProductInfoConfig()

  private def socialByAccountType(platformType: PlatformType, accountType: AccountType) =
    platformType match {
      case PlatformType.CANVAS ⇒
        accountType match {
          case AccountType.DEV ⇒ social.vk
          case AccountType.VKONTAKTE ⇒ social.vk
          case AccountType.ODNOKLASSNIKI ⇒ social.ok
          case AccountType.MOIMIR ⇒ social.mm
          case AccountType.FACEBOOK ⇒ social.vk // todo
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
      yield ProductDTO.newBuilder()
        .setId(p.id)
        .setTitle(p.title)
        .setDescription(p.description)
        .setPhotoUrl(p.photoUrl)
        .setCount(productInfo.count)
        .setPrice(productInfo.price)
        .setCurrency(productInfo.currency)
        .build()
}

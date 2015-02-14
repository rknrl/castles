//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles

import ru.rknrl.castles.database.DbConfiguration
import ru.rknrl.castles.account.AccountConfig
import ru.rknrl.castles.game._
import ru.rknrl.core.social.{Product, SocialConfigs}
import ru.rknrl.dto.AuthDTO.ProductDTO
import ru.rknrl.dto.CommonDTO.{AccountType, BuildingLevel, BuildingType}

object Config {

  class BuildingsConfig(map: Map[BuildingType, BuildingConfig]) {
    def apply(buildingType: BuildingType) = map(buildingType)
  }

  class BuildingLevelToFactor(map: Map[BuildingLevel, Double]) {
    def apply(level: BuildingLevel) = map(level)
  }

}

class Config(val host: String,
             val gamePort: Int,
             val policyPort: Int,
             val adminPort: Int,
             val httpPort: Int,
             val adminLogin: String,
             val adminPassword: String,
             val db: DbConfiguration,
             val products: List[Product],
             val social: SocialConfigs,
             val account: AccountConfig,
             val game: GameConfig) {

  private def checkProductInfoConfig() =
    for (p ← products)
      for (social ← List(social.vk, social.ok, social.mm))
        if (social.isDefined)
          if (!social.get.productsInfo.exists(_.id == p.id)) throw new Exception("can't find product info in config " + p.id)

  checkProductInfoConfig()

  private def socialByAccountType(accountType: AccountType) =
    accountType match {
      case AccountType.DEV ⇒ social.vk
      case AccountType.VKONTAKTE ⇒ social.vk
      case AccountType.ODNOKLASSNIKI ⇒ social.ok
      case AccountType.MOIMIR ⇒ social.mm
      case _ ⇒ throw new IllegalArgumentException("unknown accountType " + accountType)
    }

  def productsDto(accountType: AccountType) =
    for (p ← products;
         productInfo = socialByAccountType(accountType).get.productsInfo.find(_.id == p.id).get)
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

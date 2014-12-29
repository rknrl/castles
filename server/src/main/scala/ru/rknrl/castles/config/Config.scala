package ru.rknrl.castles.config

import ru.rknrl.castles.account.AccountConfig
import ru.rknrl.castles.game._
import ru.rknrl.core.social.Products.Products
import ru.rknrl.core.social.SocialConfigs
import ru.rknrl.dto.AuthDTO.{AccountType, ProductDTO}
import ru.rknrl.dto.CommonDTO.{BuildingLevel, BuildingType}


object Config {
  type BuildingsConfig = Map[BuildingType, BuildingConfig]
  type BuildingLevelToFactor = Map[BuildingLevel, Double]
}

class Config(val host: String,
             val gamePort: Int,
             val policyPort: Int,
             val products: Products,
             val social: SocialConfigs,
             val account: AccountConfig,
             val game: GameConfig) {
  val regeneration = 0.0002
  val speed = 0.00005

  private def checkProductInfoConfig() =
    for ((id, p) ← products)
      for (social ← List(social.vk, social.ok, social.mm))
        if (social.isDefined)
          if (!social.get.productsInfo.contains(id)) throw new Exception("can't find product info in config " + id)

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
    for ((id, p) ← products;
         productInfo = socialByAccountType(accountType).get.productsInfo(id))
    yield ProductDTO.newBuilder()
      .setId(p.id)
      .setTitle(p.title)
      .setDescription(p.description)
      .setPhotoUrl(p.photoUrl)
      .setCount(productInfo.count)
      .setPrice(productInfo.price)
      .build()
}

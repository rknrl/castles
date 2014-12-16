package ru.rknrl.castles.account

import ru.rknrl.castles.account.AccountConfig.{BuildingPrices, SkillUpgradePrices}
import ru.rknrl.dto.AccountDTO.{AccountConfigDTO, BuildingPriceDTO, SkillUpgradePriceDTO}
import ru.rknrl.dto.CommonDTO.BuildingLevel

import scala.collection.JavaConverters._

object AccountConfig {
  type BuildingPrices = Map[BuildingLevel, Int]
  type SkillUpgradePrices = Map[Int, Int]
}

class AccountConfig(val initGold: Int,
                    val initItemCount: Int,
                    val buildingPrices: BuildingPrices,
                    val skillUpgradePrices: SkillUpgradePrices,
                    val itemPrice: Int) {

  private def buildingPricesDto =
    for ((buildingLevel, price) ← buildingPrices)
    yield BuildingPriceDTO.newBuilder().setLevel(buildingLevel).setPrice(price).build()

  private def skillUpgradePricesDto =
    for ((totalLevel, price) ← skillUpgradePrices)
    yield SkillUpgradePriceDTO.newBuilder().setTotalLevel(totalLevel).setPrice(price).build()

  def dto = AccountConfigDTO.newBuilder()
    .addAllBuildings(buildingPricesDto.asJava)
    .addAllSkillUpgradePrices(skillUpgradePricesDto.asJava)
    .setItemPrice(itemPrice)
    .build()
}

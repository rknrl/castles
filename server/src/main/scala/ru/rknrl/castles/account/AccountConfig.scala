//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account

import ru.rknrl.castles.account.AccountConfig.{BuildingPrices, SkillUpgradePrices}
import ru.rknrl.dto.AccountDTO.{AccountConfigDTO, BuildingPriceDTO, SkillUpgradePriceDTO}
import ru.rknrl.dto.CommonDTO.BuildingLevel

import scala.collection.JavaConverters._

object AccountConfig {

  class BuildingPrices(val map: Map[BuildingLevel, Int]) {
    def apply(level: BuildingLevel) = map(level)
  }

  class SkillUpgradePrices(val map: Map[Int, Int]) {
    def apply(totalLevel: Int) = map(totalLevel)
  }

}

class AccountConfig(val initGold: Int,
                    val initRating: Int,
                    val initItemCount: Int,
                    val buildingPrices: BuildingPrices,
                    val skillUpgradePrices: SkillUpgradePrices,
                    val itemPrice: Int,
                    val maxAttack: Double,
                    val maxDefence: Double,
                    val maxSpeed: Double) {

  private def buildingPricesDto =
    for ((buildingLevel, price) ← buildingPrices.map)
      yield BuildingPriceDTO.newBuilder()
        .setLevel(buildingLevel)
        .setPrice(price)
        .build()

  private def skillUpgradePricesDto =
    for ((totalLevel, price) ← skillUpgradePrices.map)
      yield SkillUpgradePriceDTO.newBuilder()
        .setTotalLevel(totalLevel)
        .setPrice(price)
        .build()

  def dto = AccountConfigDTO.newBuilder()
    .addAllBuildings(buildingPricesDto.asJava)
    .addAllSkillUpgradePrices(skillUpgradePricesDto.asJava)
    .setItemPrice(itemPrice)
    .build()
}

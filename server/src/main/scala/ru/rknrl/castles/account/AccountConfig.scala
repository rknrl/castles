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

    def dto =
      for ((buildingLevel, price) ← map)
        yield BuildingPriceDTO.newBuilder
          .setLevel(buildingLevel)
          .setPrice(price)
          .build
  }

  class SkillUpgradePrices(val map: Map[Int, Int]) {
    def apply(totalLevel: Int) = map(totalLevel)

    def dto =
      for ((totalLevel, price) ← map)
        yield SkillUpgradePriceDTO.newBuilder
          .setTotalLevel(totalLevel)
          .setPrice(price)
          .build
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

  def dto = AccountConfigDTO.newBuilder
    .addAllBuildings(buildingPrices.dto.asJava)
    .addAllSkillUpgradePrices(skillUpgradePrices.dto.asJava)
    .setItemPrice(itemPrice)
    .build
}

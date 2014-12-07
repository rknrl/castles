package ru.rknrl.castles.account

import ru.rknrl.castles.account.AccountConfig.BuildingPrices
import ru.rknrl.dto.CommonDTO.BuildingLevel

object AccountConfig {
  type BuildingPrices = Map[BuildingLevel, Int]
}

class AccountConfig(val buildingPrices: BuildingPrices,
                    val itemPrice: Int,
                    val goldByDollar: Int)

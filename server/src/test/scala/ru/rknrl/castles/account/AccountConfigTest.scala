package ru.rknrl.castles.account

import ru.rknrl.dto.CommonDTO.BuildingLevel

object AccountConfigTest {
  def config = new AccountConfig(
    buildingPrices = Map(
      BuildingLevel.LEVEL_1 → 4,
      BuildingLevel.LEVEL_2 → 16,
      BuildingLevel.LEVEL_3 → 64
    ),
    skillUpgradePrices = Map(
      1 → 1,
      2 → 2,
      3 → 4,
      4 → 8,
      5 → 16,
      6 → 32,
      7 → 64,
      8 → 128,
      9 → 256
    ),
    itemPrice = 1,
    goldByDollar = 100
  )
}

class AccountConfigTest {

}

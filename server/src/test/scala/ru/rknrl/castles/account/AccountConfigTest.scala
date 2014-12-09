package ru.rknrl.castles.account

import ru.rknrl.dto.CommonDTO.BuildingLevel

object AccountConfigTest {
  def config = new AccountConfig(
    buildingPrices = Map(
      BuildingLevel.LEVEL_1 → 4,
      BuildingLevel.LEVEL_2 → 16,
      BuildingLevel.LEVEL_3 → 64
    ),
    itemPrice = 1,
    goldByDollar = 100
  )
}

class AccountConfigTest {

}

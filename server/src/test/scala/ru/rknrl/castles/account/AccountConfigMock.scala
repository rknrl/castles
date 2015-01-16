package ru.rknrl.castles.account

import ru.rknrl.castles.account.AccountConfig.{SkillUpgradePrices, BuildingPrices}
import ru.rknrl.dto.CommonDTO.BuildingLevel

object AccountConfigMock {
  def config = new AccountConfig(
    initGold = 100,
    initItemCount = 2,
    buildingPrices = new BuildingPrices(Map(
      BuildingLevel.LEVEL_1 → 4,
      BuildingLevel.LEVEL_2 → 16,
      BuildingLevel.LEVEL_3 → 64
    )),
    skillUpgradePrices = new SkillUpgradePrices(Map(
      1 → 1,
      2 → 2,
      3 → 4,
      4 → 8,
      5 → 16,
      6 → 32,
      7 → 64,
      8 → 128,
      9 → 256
    )),
    itemPrice = 1
  )
}
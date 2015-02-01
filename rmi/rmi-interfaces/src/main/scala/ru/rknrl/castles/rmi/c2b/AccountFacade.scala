package ru.rknrl.castles.rmi.c2b

import ru.rknrl.dto.AccountDTO._

abstract class AccountFacade {
  def enterGame()

  def buyBuilding(buy: BuyBuildingDTO)

  def upgradeBuilding(id: UpgradeBuildingDTO)

  def removeBuilding(id: RemoveBuildingDTO)

  def upgradeSkill(upgrade: UpgradeSkillDTO)

  def buyItem(buy: BuyItemDTO)
}

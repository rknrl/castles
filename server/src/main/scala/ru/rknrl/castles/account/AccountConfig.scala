//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account

import protos.BuildingLevel.LEVEL_1
import protos.BuildingType.{CHURCH, HOUSE, TOWER}
import protos.SkillLevel.SKILL_LEVEL_0
import protos.SkillType.{ATTACK, DEFENCE, SPEED}
import protos.SlotId._
import protos._
import ru.rknrl.Assertion
import ru.rknrl.core.Stat

class BuildingPrices(map: Map[BuildingLevel, Int]) {
  def apply(level: BuildingLevel) = map(level)

  def dto =
    for ((buildingLevel, price) ← map)
      yield BuildingPrice(buildingLevel, price)
}

class SkillUpgradePrices(map: Map[Int, Int]) {
  def apply(totalLevel: Int) = map(totalLevel)

  def dto =
    for ((totalLevel, price) ← map)
      yield SkillUpgradePrice(totalLevel, price)
}

class AccountConfig(val buildingPrices: BuildingPrices,
                    val skillUpgradePrices: SkillUpgradePrices,
                    val itemPrice: Int,
                    initGold: Int,
                    val presentGold: Int,
                    val presentInterval: Long,
                    val advertGold: Int,
                    val advertGamesInterval: Int,
                    val initRating: Double,
                    initItemCount: Int,
                    maxAttack: Double,
                    maxDefence: Double,
                    maxSpeed: Double) {

  def dto = protos.AccountConfig(
    buildings = buildingPrices.dto.toSeq,
    skillUpgradePrices = skillUpgradePrices.dto.toSeq,
    itemPrice = itemPrice,
    advertGamesInterval = advertGamesInterval
  )

  private val levelsCount = SkillLevel.values.size - 1

  /**
    * При SKILL_LEVEL_0 возвращает 1.0
    * При SKILL_LEVEL_3 возвращает maxAttack, maxDefence, maxSpeed
    */
  def skillsToStat(levels: Seq[Skill]) =
    new Stat(
      1 + levels.find(_.skillType == ATTACK).get.level.id * (maxAttack - 1) / levelsCount,
      1 + levels.find(_.skillType == DEFENCE).get.level.id * (maxDefence - 1) / levelsCount,
      1 + levels.find(_.skillType == SPEED).get.level.id * (maxSpeed - 1) / levelsCount
    )

  private def initSlots =
    List(
      Slot(SLOT_1, None),
      Slot(SLOT_2, None),
      Slot(SLOT_3, Some(BuildingPrototype(HOUSE, LEVEL_1))),
      Slot(SLOT_4, Some(BuildingPrototype(TOWER, LEVEL_1))),
      Slot(SLOT_5, Some(BuildingPrototype(CHURCH, LEVEL_1)))
    )

  private def initSkills =
    SkillType.values.map(Skill(_, SKILL_LEVEL_0))

  private def initItems =
    ItemType.values.map(Item(_, initItemCount))

  def initState =
    protos.AccountState(
      slots = initSlots,
      skills = initSkills,
      items = initItems,
      gold = initGold,
      gamesCount = 0
    )
}

object AccountConfig {
  def nextBuildingLevel(level: BuildingLevel) =
    level match {
      case BuildingLevel.LEVEL_1 ⇒ BuildingLevel.LEVEL_2
      case BuildingLevel.LEVEL_2 ⇒ BuildingLevel.LEVEL_3
    }

  def nextSkillLevel(level: SkillLevel) =
    level match {
      case SkillLevel.SKILL_LEVEL_0 ⇒ SkillLevel.SKILL_LEVEL_1
      case SkillLevel.SKILL_LEVEL_1 ⇒ SkillLevel.SKILL_LEVEL_2
      case SkillLevel.SKILL_LEVEL_2 ⇒ SkillLevel.SKILL_LEVEL_3
    }


  private def isLastTotalLevel(levels: Seq[Skill]) = getTotalLevel(levels) == 9

  def nextTotalLevel(levels: Seq[Skill]) = {
    Assertion.check(!isLastTotalLevel(levels))
    getTotalLevel(levels) + 1
  }

  def getTotalLevel(skillLevels: Seq[Skill]) = {
    var total = 0
    for (skillLevel ← skillLevels) total += skillLevel.level.id
    total
  }
}

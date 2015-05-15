//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account

import ru.rknrl.Assertion
import ru.rknrl.core.Stat
import ru.rknrl.dto.BuildingLevel.LEVEL_1
import ru.rknrl.dto.BuildingType.{CHURCH, HOUSE, TOWER}
import ru.rknrl.dto.SkillLevel.SKILL_LEVEL_0
import ru.rknrl.dto.SkillType.{ATTACK, DEFENCE, SPEED}
import ru.rknrl.dto.SlotId._
import ru.rknrl.dto._

class BuildingPrices(map: Map[BuildingLevel, Int]) {
  def apply(level: BuildingLevel) = map(level)

  def dto =
    for ((buildingLevel, price) ← map)
      yield BuildingPriceDTO(buildingLevel, price)
}

class SkillUpgradePrices(map: Map[Int, Int]) {
  def apply(totalLevel: Int) = map(totalLevel)

  def dto =
    for ((totalLevel, price) ← map)
      yield SkillUpgradePriceDTO(totalLevel, price)
}

class AccountConfig(val buildingPrices: BuildingPrices,
                    val skillUpgradePrices: SkillUpgradePrices,
                    val itemPrice: Int,
                    initGold: Int,
                    val initRating: Double,
                    initItemCount: Int,
                    maxAttack: Double,
                    maxDefence: Double,
                    maxSpeed: Double) {

  def dto = AccountConfigDTO(
    buildingPrices.dto.toSeq,
    skillUpgradePrices.dto.toSeq,
    itemPrice
  )

  private val levelsCount = SkillLevel.values.size - 1

  /**
   * При SKILL_LEVEL_0 возвращает 1.0
   * При SKILL_LEVEL_3 возвращает maxAttack, maxDefence, maxSpeed
   */
  def skillsToStat(levels: Seq[SkillLevelDTO]) =
    new Stat(
      1 + levels.find(_.skillType == ATTACK).get.level.id * (maxAttack - 1) / levelsCount,
      1 + levels.find(_.skillType == DEFENCE).get.level.id * (maxDefence - 1) / levelsCount,
      1 + levels.find(_.skillType == SPEED).get.level.id * (maxSpeed - 1) / levelsCount
    )

  private def initSlots =
    List(
      SlotDTO(SLOT_1, None),
      SlotDTO(SLOT_2, None),
      SlotDTO(SLOT_3, Some(BuildingPrototype(HOUSE, LEVEL_1))),
      SlotDTO(SLOT_4, Some(BuildingPrototype(TOWER, LEVEL_1))),
      SlotDTO(SLOT_5, Some(BuildingPrototype(CHURCH, LEVEL_1)))
    )

  private def initSkills =
    SkillType.values.map(SkillLevelDTO(_, SKILL_LEVEL_0))

  private def initItems =
    ItemType.values.map(ItemDTO(_, initItemCount))

  def initState =
    AccountStateDTO(
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


  private def isLastTotalLevel(levels: Seq[SkillLevelDTO]) = getTotalLevel(levels) == 9

  def nextTotalLevel(levels: Seq[SkillLevelDTO]) = {
    Assertion.check(!isLastTotalLevel(levels))
    getTotalLevel(levels) + 1
  }

  def getTotalLevel(skillLevels: Seq[SkillLevelDTO]) = {
    var total = 0
    for (skillLevel ← skillLevels) total += skillLevel.level.id
    total
  }
}

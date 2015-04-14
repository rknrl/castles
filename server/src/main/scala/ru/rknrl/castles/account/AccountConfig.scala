//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account

import ru.rknrl.Assertion
import ru.rknrl.castles.account.AccountState.{Items, Slots, Skills}
import ru.rknrl.castles.game.state.Stat
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
                    initRating: Double,
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
  def skillsToStat(levels: Skills) =
    new Stat(
      1 + levels(ATTACK).id * (maxAttack - 1) / levelsCount,
      1 + levels(DEFENCE).id * (maxDefence - 1) / levelsCount,
      1 + levels(SPEED).id * (maxSpeed - 1) / levelsCount
    )

  private def initSlots: Slots =
    Map(
      SLOT_1 → None,
      SLOT_2 → None,
      SLOT_3 → Some(BuildingPrototype(HOUSE, LEVEL_1)),
      SLOT_4 → Some(BuildingPrototype(TOWER, LEVEL_1)),
      SLOT_5 → Some(BuildingPrototype(CHURCH, LEVEL_1))
    )

  private def initSkills: Skills =
    SkillType.values.map(_ → SKILL_LEVEL_0).toMap

  private def initItems: Items =
    ItemType.values.map(_ → initItemCount).toMap

  def initAccount = new AccountState(
    slots = initSlots,
    skills = initSkills,
    items = initItems,
    gold = initGold,
    rating = initRating,
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

  private def totalLevel(levels: Skills) = {
    var total = 0
    for ((skillType, level) ← levels) total += level.id
    total
  }

  private def isLastTotalLevel(levels: Skills) = totalLevel(levels) == 9

  def nextTotalLevel(levels: Skills) = {
    Assertion.check(!isLastTotalLevel(levels))
    totalLevel(levels) + 1
  }
}

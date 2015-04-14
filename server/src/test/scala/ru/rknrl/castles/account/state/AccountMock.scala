//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account.state

import ru.rknrl.castles.account.AccountState.{Items, Skills, Slots}
import ru.rknrl.castles.account.{AccountConfig, AccountState, BuildingPrices, SkillUpgradePrices}
import ru.rknrl.dto.BuildingLevel._
import ru.rknrl.dto.BuildingType.{CHURCH, HOUSE, TOWER}
import ru.rknrl.dto.SkillLevel.SKILL_LEVEL_0
import ru.rknrl.dto.SlotId._
import ru.rknrl.dto.{BuildingLevel, BuildingPrototype, ItemType, SkillType}

object AccountMock {
  def buildingPrices: Map[BuildingLevel, Int] = Map(
    LEVEL_1 → 4,
    LEVEL_2 → 16,
    LEVEL_3 → 64
  )

  def skillUpgradePrices =
    for (i ← 0 to 8) yield (i + 1) → Math.pow(2, i).toInt

  def config(initGold: Int = 10,
             initRating: Double = 1400,
             initItemCount: Int = 4,
             buildingPrices: Map[BuildingLevel, Int] = buildingPrices,
             skillUpgradePrices: Map[Int, Int] = skillUpgradePrices.toMap,
             itemPrice: Int = 1,
             maxAttack: Double = 3.0,
             maxDefence: Double = 3.0,
             maxSpeed: Double = 1.2) =
    new AccountConfig(
      initGold = initGold,
      initRating = initRating,
      initItemCount = initItemCount,
      buildingPrices = new BuildingPrices(buildingPrices),
      skillUpgradePrices = new SkillUpgradePrices(skillUpgradePrices),
      itemPrice = itemPrice,
      maxAttack = maxAttack,
      maxDefence = maxDefence,
      maxSpeed = maxSpeed
    )

  def slots: Slots =
    Map(
      SLOT_1 → None,
      SLOT_2 → None,
      SLOT_3 → Some(BuildingPrototype(HOUSE, LEVEL_1)),
      SLOT_4 → Some(BuildingPrototype(TOWER, LEVEL_1)),
      SLOT_5 → Some(BuildingPrototype(CHURCH, LEVEL_1))
    )

  def skills: Skills =
    SkillType.values.map(_ → SKILL_LEVEL_0).toMap

  def items: Items =
    ItemType.values.map(_ → 4).toMap

  def accountState(slots: Slots = slots,
                   skills: Skills = skills,
                   items: Items = items,
                   gold: Int = 10,
                   rating: Double = 1400,
                   gamesCount: Int = 0) =
    new AccountState(
      slots = slots,
      skills = skills,
      items = items,
      gold = gold,
      rating = rating,
      gamesCount = gamesCount
    )
}

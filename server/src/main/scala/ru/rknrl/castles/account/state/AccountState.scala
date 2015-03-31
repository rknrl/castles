//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account.state

import ru.rknrl.Assertion
import ru.rknrl.castles.account.AccountConfig
import ru.rknrl.core.social.Product
import ru.rknrl.dto.AccountDTO._
import ru.rknrl.dto.CommonDTO.BuildingLevel._
import ru.rknrl.dto.CommonDTO.BuildingType._
import ru.rknrl.dto.CommonDTO.SkillLevel._
import ru.rknrl.dto.CommonDTO.SlotId._
import ru.rknrl.dto.CommonDTO._

import scala.collection.JavaConverters._

class AccountState(val slots: Slots,
                   val skills: Skills,
                   val items: Items,
                   val gold: Int,
                   val rating: Double,
                   val gamesCount: Int) {

  def buyBuilding(id: SlotId, buildingType: BuildingType, config: AccountConfig) = {
    val price = config.buildingPrices(LEVEL_1)
    Assertion.check(price <= gold)

    setBuilding(id, BuildingPrototype(buildingType, LEVEL_1))
      .addGold(-price)
  }

  def upgradeBuilding(id: SlotId, config: AccountConfig) = {
    val upgraded = BuildingPrototype.upgraded(slots(id).getBuildingPrototype)
    val price = config.buildingPrices(upgraded.getLevel)
    Assertion.check(price <= gold)

    setBuilding(id, upgraded)
      .addGold(-price)
  }

  def removeBuilding(id: SlotId): AccountState =
    setBuilding(id, None)

  def setBuilding(id: SlotId, buildingPrototype: BuildingPrototypeDTO): AccountState =
    setBuilding(id, Some(buildingPrototype))

  def setBuilding(id: SlotId, buildingPrototype: Option[BuildingPrototypeDTO]): AccountState = {
    val newSlot = Slot(id, buildingPrototype)
    copy(newSlots = slots.updated(id, newSlot))
  }

  def upgradeSkill(skillType: SkillType, config: AccountConfig) = {
    val price = config.skillUpgradePrices(skills.nextTotalLevel)
    Assertion.check(price <= gold)

    val nextLevel = Skills.nextLevel(skills.levels(skillType))

    setSkill(skillType, nextLevel)
      .addGold(-price)
  }

  def setSkill(skillType: SkillType, skillLevel: SkillLevel) =
    copy(newSkills = skills.updated(skillType, skillLevel))

  def buyItem(itemType: ItemType, config: AccountConfig) = {
    val price = config.itemPrice
    Assertion.check(price <= gold)

    addItem(itemType, 1)
      .addGold(-price)
  }

  def addItem(itemType: ItemType, count: Int) = {
    val newCount = Math.max(0, items(itemType).count + count)
    val newItem = new Item(itemType, newCount)
    copy(newItems = items.updated(itemType, newItem))
  }

  def addGold(value: Int) =
    copy(newGold = Math.max(0, gold + value))

  def incGamesCount = copy(newGamesCount = gamesCount + 1)

  def setNewRating(newRating: Double) = copy(newRating = newRating)

  private def copy(newSlots: Slots = slots,
                   newSkills: Skills = skills,
                   newItems: Items = items,
                   newGold: Int = gold,
                   newRating: Double = rating,
                   newGamesCount: Int = gamesCount) =
    new AccountState(newSlots, newSkills, newItems, newGold, newRating, newGamesCount)

  def applyProduct(product: Product, count: Int) =
    product.id match {
      case ProductId.STARS_VALUE ⇒ addGold(count)
      case _ ⇒ throw new IllegalArgumentException("unknown product id " + product.id)
    }

  def dto = AccountStateDTO.newBuilder
    .addAllSlots(slots.dto.asJava)
    .addAllSkills(skills.dto.asJava)
    .addAllItems(items.dto.asJava)
    .setGold(gold)
    .setRating(rating)
    .setGamesCount(gamesCount)
    .build
}

object AccountState {
  private def initSlotBuildings =
    List(
      Slot.empty(SLOT_1),
      Slot.empty(SLOT_2),
      Slot(SLOT_3, BuildingPrototype(HOUSE, LEVEL_1)),
      Slot(SLOT_4, BuildingPrototype(TOWER, LEVEL_1)),
      Slot(SLOT_5, BuildingPrototype(CHURCH, LEVEL_1))
    )

  private def initSlotsMap =
    initSlotBuildings.map(slot ⇒ slot.getId → slot).toMap

  private def initSlots = new Slots(initSlotsMap)

  private def initSkillLevels =
    SkillType.values.map(_ → SKILL_LEVEL_0).toMap

  private def initSkills = new Skills(initSkillLevels)

  private def initItemsCount(config: AccountConfig) =
    for (itemType ← ItemType.values)
      yield itemType → new Item(itemType, config.initItemCount)

  private def initItems(config: AccountConfig) = new Items(initItemsCount(config).toMap)

  def initAccount(config: AccountConfig) = new AccountState(
    slots = initSlots,
    skills = initSkills,
    items = initItems(config),
    gold = config.initGold,
    rating = config.initRating,
    gamesCount = 0
  )

  def apply(dto: AccountStateDTO) = new AccountState(
    slots = Slots(dto.getSlotsList.asScala),
    skills = Skills(dto.getSkillsList.asScala),
    items = Items(dto.getItemsList.asScala),
    gold = dto.getGold,
    rating = dto.getRating,
    gamesCount = dto.getGamesCount
  )
}
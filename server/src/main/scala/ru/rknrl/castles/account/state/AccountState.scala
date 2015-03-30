//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account.state

import ru.rknrl.castles.account.AccountConfig
import ru.rknrl.core.social.Product
import ru.rknrl.dto.AccountDTO._
import ru.rknrl.dto.CommonDTO._

import scala.collection.JavaConverters._

class AccountState(val slots: Slots,
                   val skills: Skills,
                   val items: Items,
                   val gold: Int,
                   val rating: Double,
                   val gamesCount: Int) {

  def buyBuilding(id: SlotId, buildingType: BuildingType, config: AccountConfig) = {
    val price = config.buildingPrices(BuildingLevel.LEVEL_1)
    assert(price <= gold)
    copy(newSlots = slots.build(id, buildingType), newGold = gold - price)
  }

  def upgradeBuilding(id: SlotId, config: AccountConfig) = {
    val upgraded = slots(id).upgrade
    val price = config.buildingPrices(upgraded.buildingPrototype.get.level)
    assert(price <= gold)
    copy(newSlots = slots.upgrade(id), newGold = gold - price)
  }

  def setBuilding(id: SlotId, buildingPrototype: BuildingPrototype) =
    copy(newSlots = slots.set(id, buildingPrototype))

  def removeBuilding(id: SlotId): AccountState =
    copy(newSlots = slots.remove(id))

  def upgradeSkill(skillType: SkillType, config: AccountConfig) = {
    val price = config.skillUpgradePrices(skills.nextTotalLevel)
    assert(price <= gold)
    copy(newSkills = skills.upgrade(skillType), newGold = gold - price)
  }

  def setSkill(skillType: SkillType, skillLevel: SkillLevel) =
    copy(newSkills = skills.set(skillType, skillLevel))

  def buyItem(itemType: ItemType, config: AccountConfig) = {
    val price = config.itemPrice
    assert(price <= gold)
    copy(newItems = items.add(itemType, 1), newGold = gold - price)
  }

  def addItem(itemType: ItemType, count: Int) =
    copy(newItems = items.add(itemType, count))

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

  def dto = AccountStateDTO.newBuilder()
    .addAllSlots(slots.dto.asJava)
    .addAllSkills(skills.dto.asJava)
    .addAllItems(items.dto.asJava)
    .setGold(gold)
    .setRating(rating)
    .setGamesCount(gamesCount)
    .build()
}

object AccountState {
  private def initSlotBuildings =
    List(
      Slot.empty(SlotId.SLOT_1),
      Slot.empty(SlotId.SLOT_2),
      Slot(SlotId.SLOT_3, BuildingPrototype(BuildingType.HOUSE, BuildingLevel.LEVEL_1)),
      Slot(SlotId.SLOT_4, BuildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_1)),
      Slot(SlotId.SLOT_5, BuildingPrototype(BuildingType.CHURCH, BuildingLevel.LEVEL_1))
    )

  private def initSlotsMap =
    initSlotBuildings.map(slot ⇒ slot.id → slot).toMap

  private def initSlots = new Slots(initSlotsMap)

  private def initSkillLevels =
    SkillType.values.map(_ → SkillLevel.SKILL_LEVEL_0).toMap

  private def initSkills = new Skills(initSkillLevels)

  private def initItemsCount(config: AccountConfig) =
    for (itemType ← ItemType.values())
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
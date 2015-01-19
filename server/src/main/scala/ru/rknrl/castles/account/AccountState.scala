package ru.rknrl.castles.account

import ru.rknrl.castles.account.objects._
import ru.rknrl.dto.AccountDTO._
import ru.rknrl.dto.CommonDTO._

class AccountState(val startLocation: StartLocation,
                   val skills: Skills,
                   val items: Items,
                   val gold: Int,
                   val config: AccountConfig) {

  def swapSlots(id1: SlotId, id2: SlotId): AccountState =
    copy(newStartLocation = startLocation.swap(id1, id2))

  def buyBuilding(id: SlotId, buildingType: BuildingType): AccountState = {
    val price = config.buildingPrices(BuildingLevel.LEVEL_1)
    assert(price <= gold)
    copy(newStartLocation = startLocation.buy(id, buildingType), newGold = gold - price)
  }

  def upgradeBuilding(id: SlotId): AccountState = {
    val level = BuildingPrototype.getNextLevel(startLocation.getLevel(id))
    val price = config.buildingPrices(level)
    assert(price <= gold)
    copy(newStartLocation = startLocation.upgrade(id), newGold = gold - price)
  }

  def removeBuilding(id: SlotId): AccountState =
    copy(newStartLocation = startLocation.remove(id))

  def upgradeSkill(skillType: SkillType, config: AccountConfig) = {
    val price = config.skillUpgradePrices(skills.nextTotalLevel)
    assert(price <= gold)
    copy(newSkills = skills.upgrade(skillType), newGold = gold - price)
  }

  def buyItem(itemType: ItemType) = {
    val price = config.itemPrice
    assert(price <= gold)
    copy(newItems = items.add(itemType, 1), newGold = gold - price)
  }

  def addItem(itemType: ItemType, count: Int) = {
    copy(newItems = items.add(itemType, count))
  }

  def addGold(value: Int) = {
    assert(gold + value >= 0)
    copy(newGold = gold + value)
  }

  private def copy(newStartLocation: StartLocation = startLocation,
                   newSkills: Skills = skills,
                   newItems: Items = items,
                   newGold: Int = gold) =
    new AccountState(newStartLocation, newSkills, newItems, newGold, config)

  def dto = AccountStateDTO.newBuilder()
    .setStartLocation(startLocation.dto)
    .setSkills(skills.dto)
    .setItems(items.dto)
    .setGold(gold)
    .build()
}

object AccountState {
  private def initSlotBuildings =
    Map(
      SlotId.SLOT_1 → None,
      SlotId.SLOT_2 → None,
      SlotId.SLOT_3 → Some(new BuildingPrototype(BuildingType.HOUSE, BuildingLevel.LEVEL_1)),
      SlotId.SLOT_4 → Some(new BuildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_1)),
      SlotId.SLOT_5 → Some(new BuildingPrototype(BuildingType.CHURCH, BuildingLevel.LEVEL_1))
    )

  private def initSlots =
    for ((slotId, building) ← initSlotBuildings)
    yield slotId → new StartLocationSlot(slotId, building)

  private def initStartLocation = new StartLocation(initSlots)

  private def initSkillLevels =
    for (skillType ← SkillType.values())
    yield skillType → SkillLevel.SKILL_LEVEL_0

  private def initSkills = new Skills(initSkillLevels.toMap)

  private def initItemsCount(config: AccountConfig) = for (itemType ← ItemType.values()) yield itemType → new Item(itemType, config.initItemCount)

  private def initItems(config: AccountConfig) = new Items(initItemsCount(config).toMap)

  def initAccount(config: AccountConfig) = new AccountState(initStartLocation, initSkills, initItems(config), config.initGold, config)

  def fromDto(dto: AccountStateDTO, config: AccountConfig) = new AccountState(
    StartLocation.fromDto(dto.getStartLocation),
    Skills.fromDto(dto.getSkills),
    Items.fromDto(dto.getItems),
    dto.getGold,
    config
  )
}
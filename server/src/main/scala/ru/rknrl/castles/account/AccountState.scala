//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account

import protos.BuildingLevel.LEVEL_1
import protos._
import ru.rknrl.Assertion
import ru.rknrl.castles.database.Calendar

object AccountState {
  def buyBuilding(stateOption: Option[AccountState], id: SlotId, buildingType: BuildingType, config: AccountConfig) = {
    val state = stateOption.getOrElse(config.initState)
    val price = config.buildingPrices(LEVEL_1)
    Assertion.check(price <= state.gold)

    val oldSlot = state.slots.find(_.id == id).get
    Assertion.check(oldSlot.buildingPrototype.isEmpty)
    val newSlot = oldSlot.copy(buildingPrototype = Some(BuildingPrototype(buildingType, LEVEL_1)))

    state.copy(
      slots = replace(state.slots, oldSlot, newSlot),
      gold = state.gold - price
    )
  }

  def upgradeBuilding(stateOption: Option[AccountState], id: SlotId, config: AccountConfig) = {
    val state = stateOption.getOrElse(config.initState)
    val oldSlot = state.slots.find(_.id == id).get
    val nextLevel = AccountConfig.nextBuildingLevel(oldSlot.buildingPrototype.get.buildingLevel)
    val price = config.buildingPrices(nextLevel)
    Assertion.check(price <= state.gold)

    val newSlot = oldSlot.copy(buildingPrototype = Some(BuildingPrototype(oldSlot.buildingPrototype.get.buildingType, nextLevel)))

    state.copy(
      slots = replace(state.slots, oldSlot, newSlot),
      gold = state.gold - price
    )
  }

  def removeBuilding(stateOption: Option[AccountState], id: SlotId, config: AccountConfig) = {
    val state = stateOption.getOrElse(config.initState)
    val buildingsCount = state.slots.count(_.buildingPrototype.isDefined)
    Assertion.check(buildingsCount > 1)

    val oldSlot = state.slots.find(_.id == id).get
    Assertion.check(oldSlot.buildingPrototype.isDefined)
    val newSlot = oldSlot.copy(buildingPrototype = None)

    state.copy(slots = replace(state.slots, oldSlot, newSlot))
  }

  def upgradeSkill(stateOption: Option[AccountState], skillType: SkillType, config: AccountConfig) = {
    val state = stateOption.getOrElse(config.initState)
    val price = config.skillUpgradePrices(AccountConfig.getTotalLevel(state.skills) + 1)
    Assertion.check(price <= state.gold)

    val oldSkill = state.skills.find(_.skillType == skillType).get
    val newLevel = AccountConfig.nextSkillLevel(oldSkill.level)
    val newSkill = oldSkill.copy(level = newLevel)

    state.copy(
      skills = replace(state.skills, oldSkill, newSkill),
      gold = state.gold - price
    )
  }

  def buyItem(stateOption: Option[AccountState], itemType: ItemType, config: AccountConfig) = {
    val state = stateOption.getOrElse(config.initState)
    Assertion.check(config.itemPrice <= state.gold)

    addGold(
      addItem(state, itemType, 1),
      -config.itemPrice
    )
  }

  def addItem(state: AccountState, itemType: ItemType, count: Int) = {
    val oldItem = state.items.find(_.itemType == itemType).get
    val newCount = Math.max(0, oldItem.count + count)
    val newItem = oldItem.copy(count = newCount)
    state.copy(items = replace(state.items, oldItem, newItem))
  }

  def addGold(state: AccountState, amount: Int) =
    state.copy(gold = Math.max(0, state.gold + amount))

  def incGamesCount(state: AccountState) =
    state.copy(gamesCount = state.gamesCount + 1)

  def applyUsedItems(state: AccountState, usedItems: Map[ItemType, Int]) = {
    var s = state
    for ((itemType, count) ← usedItems)
      s = addItem(s, itemType, -count)
    s
  }

  def applyProduct(state: AccountState, product: ru.rknrl.core.social.Product, count: Int) =
    product.id match {
      case ProductId.STARS.id ⇒ addGold(state, count)
    }

  private def replace[T](xs: Seq[T], oldValue: T, newValue: T) = {
    val index = xs.indexOf(oldValue)
    xs.updated(index, newValue)
  }

  def acceptPresent(stateOption: Option[AccountState], config: AccountConfig, calendar: Calendar) = {
    val state = stateOption.getOrElse(config.initState)
    val time = calendar.getCurrentMillis
    if (time - state.lastPresentTime.getOrElse(0L) > config.presentInterval)
      state.copy(
        lastPresentTime = Some(time),
        gold = state.gold + config.presentGold
      )
    else
      state
  }

  def acceptAdvert(stateOption: Option[AccountState], accepted: Boolean, config: AccountConfig) = {
    val state = stateOption.getOrElse(config.initState)
    if (!accepted)
      state.copy(
        lastGamesCountAdvert = Some(state.gamesCount)
      )
    else if (state.gamesCount - state.lastGamesCountAdvert.getOrElse(0) >= config.advertGamesInterval)
      state.copy(
        lastGamesCountAdvert = Some(state.gamesCount),
        gold = state.gold + config.advertGold
      )
    else
      state
  }

  def acceptWeekTop(stateOption: Option[AccountState], config: AccountConfig, weekNumber: Int) = {
    val state = stateOption.getOrElse(config.initState)
    if (state.weekNumberAccepted.getOrElse(0) < weekNumber)
      state.copy(
        weekNumberAccepted = Some(weekNumber)
      )
    else
      state
  }
}

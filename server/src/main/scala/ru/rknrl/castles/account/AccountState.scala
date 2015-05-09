//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account

import ru.rknrl.Assertion
import ru.rknrl.castles.account.AccountState.{Items, Skills, Slots}
import ru.rknrl.core.social.Product
import ru.rknrl.dto.BuildingLevel.LEVEL_1
import ru.rknrl.dto._

case class AccountState(slots: Slots,
                        skills: Skills,
                        items: Items,
                        gold: Int,
                        rating: Double,
                        gamesCount: Int) {

  def buyBuilding(id: SlotId, buildingType: BuildingType, config: AccountConfig) = {
    val price = config.buildingPrices(LEVEL_1)
    Assertion.check(price <= gold)
    Assertion.check(slots(id).isEmpty)

    setBuilding(id, BuildingPrototype(buildingType, LEVEL_1))
      .addGold(-price)
  }

  def upgradeBuilding(id: SlotId, config: AccountConfig) = {
    val oldPrototype = slots(id).get
    val nextLevel = AccountConfig.nextBuildingLevel(oldPrototype.buildingLevel)
    val price = config.buildingPrices(nextLevel)
    Assertion.check(price <= gold)

    setBuilding(id, BuildingPrototype(oldPrototype.buildingType, nextLevel))
      .addGold(-price)
  }

  def removeBuilding(id: SlotId): AccountState = {
    Assertion.check(slots(id).isDefined)
    val buildingsCount = slots.values.count(_.isDefined)
    Assertion.check(buildingsCount > 1)

    setBuilding(id, None)
  }

  def setBuilding(id: SlotId, buildingPrototype: BuildingPrototype): AccountState =
    setBuilding(id, Some(buildingPrototype))

  private def setBuilding(id: SlotId, buildingPrototype: Option[BuildingPrototype]): AccountState =
    copy(newSlots = slots.updated(id, buildingPrototype))

  def upgradeSkill(skillType: SkillType, config: AccountConfig) = {
    val price = config.skillUpgradePrices(AccountConfig.nextTotalLevel(skills))
    Assertion.check(price <= gold)

    val nextLevel = AccountConfig.nextSkillLevel(skills(skillType))

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
    val newCount = Math.max(0, items(itemType) + count)
    copy(newItems = items.updated(itemType, newCount))
  }

  def addGold(value: Int) =
    copy(newGold = Math.max(0, gold + value))

  def incGamesCount = copy(newGamesCount = gamesCount + 1)

  def setNewRating(newRating: Double) = copy(newRating = newRating)

  def applyUsedItems(usedItems: Map[ItemType, Int]) = {
    var state = this
    for ((itemType, count) ← usedItems)
      state = state.addItem(itemType, -count)
    state
  }

  private def copy(newSlots: Slots = slots,
                   newSkills: Skills = skills,
                   newItems: Items = items,
                   newGold: Int = gold,
                   newRating: Double = rating,
                   newGamesCount: Int = gamesCount) =
    new AccountState(newSlots, newSkills, newItems, newGold, newRating, newGamesCount)

  def applyProduct(product: Product, count: Int) =
    product.id match {
      case ProductId.STARS.id ⇒ addGold(count)
    }

  private def itemsDto =
    for ((itemType, count) ← items)
      yield ItemDTO(itemType, count)

  private def slotsDto =
    for ((slotId, buildingPrototype) ← slots)
      yield SlotDTO(slotId, buildingPrototype)

  private def skillsDto =
    for ((skillType, level) ← skills)
      yield SkillLevelDTO(skillType, level)

  def dto = AccountStateDTO(
    slotsDto.toSeq,
    skillsDto.toSeq,
    itemsDto.toSeq,
    gold = gold,
    gamesCount = gamesCount
  )
}

object AccountState {
  type Items = Map[ItemType, Int]
  type Slots = Map[SlotId, Option[BuildingPrototype]]
  type Skills = Map[SkillType, SkillLevel]

  private def slots(dto: Seq[SlotDTO]) =
    dto.map(s ⇒ s.id → s.buildingPrototype).toMap

  private def items(dto: Seq[ItemDTO]) =
    dto.map(i ⇒ i.itemType → i.count).toMap

  private def skills(dto: Seq[SkillLevelDTO]) =
    dto.map(s ⇒ s.skillType → s.level).toMap

  def fromDto(dto: AccountStateDTO, rating: Double) = new AccountState(
    slots = slots(dto.slots),
    skills = skills(dto.skills),
    items = items(dto.items),
    gold = dto.gold,
    rating = rating,
    gamesCount = dto.gamesCount
  )
}
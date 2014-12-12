package ru.rknrl.castles.account

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.account.objects.items.ItemsTest
import ru.rknrl.castles.account.objects.skills.SkillsTest
import ru.rknrl.castles.account.objects.startLocation.StartLocationTest
import ru.rknrl.castles.account.objects.{BuildingPrototype, Items, Skills, StartLocation}
import ru.rknrl.dto.AccountDTO.PricesDTO
import ru.rknrl.dto.CommonDTO._

object AccountStateTest {
  def accountState(startLocation: StartLocation = StartLocationTest.startLocation1,
                   skills: Skills = SkillsTest.skills,
                   items: Items = ItemsTest.items,
                   gold: Int = 666,
                   config: AccountConfig = AccountConfigTest.config) =
    new AccountState(
      startLocation,
      skills,
      items,
      gold,
      config
    )
}

class AccountStateTest extends FlatSpec with Matchers {

  import ru.rknrl.castles.account.AccountStateTest._

  "swapSlots" should "change startLocation & not change others" in {
    val state = accountState()
    val updated = state.swapSlots(SlotId.SLOT_1, SlotId.SLOT_2)
    updated.startLocation.slots(SlotId.SLOT_1).buildingPrototype should be(state.startLocation.slots(SlotId.SLOT_2).buildingPrototype)
    updated.startLocation.slots(SlotId.SLOT_2).buildingPrototype should be(state.startLocation.slots(SlotId.SLOT_1).buildingPrototype)

    updated.skills should be(state.skills)
    updated.items should be(state.items)
    updated.gold should be(state.gold)
    updated.config should be(state.config)
  }

  "buyBuilding" should "throw AssertionError if price < gold" in {
    a[AssertionError] should be thrownBy {
      accountState(gold = 0).buyBuilding(SlotId.SLOT_2, BuildingType.HOUSE)
    }
  }

  "buyBuilding" should "change startLocation and gold & not change others" in {
    val state = accountState(gold = 666)
    val updated = state.buyBuilding(SlotId.SLOT_2, BuildingType.HOUSE)
    updated.startLocation.slots(SlotId.SLOT_1).buildingPrototype should be(state.startLocation.slots(SlotId.SLOT_1).buildingPrototype)
    updated.startLocation.slots(SlotId.SLOT_2).buildingPrototype.get should be(new BuildingPrototype(BuildingType.HOUSE, BuildingLevel.LEVEL_1))

    updated.gold should be(666 - state.config.buildingPrices(BuildingLevel.LEVEL_1))

    updated.skills should be(state.skills)
    updated.items should be(state.items)
    updated.config should be(state.config)
  }

  "upgradeBuilding" should "throw AssertionError if price < gold" in {
    a[AssertionError] should be thrownBy {
      accountState(gold = 0).upgradeBuilding(SlotId.SLOT_1)
    }
  }

  "upgradeBuilding" should "change startLocation and gold & not change others" in {
    val state = accountState(gold = 666)
    val updated = state.upgradeBuilding(SlotId.SLOT_1)
    updated.startLocation.slots(SlotId.SLOT_1).buildingPrototype.get.level should be(BuildingLevel.LEVEL_3)

    updated.gold should be(666 - state.config.buildingPrices(BuildingLevel.LEVEL_3))

    updated.skills should be(state.skills)
    updated.items should be(state.items)
    updated.config should be(state.config)
  }

  "removeBuilding" should "change startLocation & not change others" in {
    val state = accountState(startLocation = StartLocationTest.startLocation2)
    val updated = state.removeBuilding(SlotId.SLOT_1)
    updated.startLocation.slots(SlotId.SLOT_1).buildingPrototype should be(None)

    updated.skills should be(state.skills)
    updated.items should be(state.items)
    updated.gold should be(state.gold)
    updated.config should be(state.config)
  }

  "upgradeSkill" should "throw AssertionError if price < gold" in {
    a[AssertionError] should be thrownBy {
      accountState(gold = 0).upgradeSkill(SkillType.ATTACK)
    }
  }

  "upgradeSkill" should "change startLocation and gold & not change others" in {
    val state = accountState(gold = 666)
    val updated = state.upgradeSkill(SkillType.ATTACK)
    updated.skills.levels(SkillType.ATTACK) should be(SkillLevel.SKILL_LEVEL_2)

    updated.gold should be(666 - state.skills.upgradePrice)

    updated.startLocation should be(state.startLocation)
    updated.items should be(state.items)
    updated.config should be(state.config)
  }

  "addItem" should "throw AssertionError if price < gold" in {
    a[AssertionError] should be thrownBy {
      accountState(gold = 0).buyItem(ItemType.TORNADO)
    }
  }

  "addItem" should "change item and gold & not change others" in {
    val state = accountState(gold = 666)
    val updated = state.buyItem(ItemType.TORNADO)

    updated.items.items(ItemType.TORNADO).count should be(state.items.items(ItemType.TORNADO).count + 1)
    updated.gold should be(666 - state.config.itemPrice)

    updated.startLocation should be(state.startLocation)
    updated.skills should be(state.skills)
    updated.config should be(state.config)
  }

  "addGold" should "throw AssertionError if gold < value" in {
    a[AssertionError] should be thrownBy {
      accountState(gold = 0).addGold(-2)
    }
  }

  "addGold" should "change gold & not change others" in {
    val state = accountState(gold = 10)
    val updated = state.addGold(20)

    updated.gold should be(30)

    updated.startLocation should be(state.startLocation)
    updated.skills should be(state.skills)
    updated.items should be(state.items)
    updated.config should be(state.config)
  }

  def checkPrices(state: AccountState, dto: PricesDTO) = {
    dto.getBuildingsCount should be(3)
    dto.getBuildings(0).getPrice should be(state.config.buildingPrices(BuildingLevel.LEVEL_1))
    dto.getBuildings(1).getPrice should be(state.config.buildingPrices(BuildingLevel.LEVEL_2))
    dto.getBuildings(2).getPrice should be(state.config.buildingPrices(BuildingLevel.LEVEL_3))
    dto.getSkillsUpgradePrice should be(state.skills.upgradePrice)
    dto.getItemPrice should be(state.config.itemPrice)
    dto.getGoldByDollar should be(state.config.goldByDollar)
  }

  "pricesDto" should "be correct" in {
    val state = accountState()
    checkPrices(state, state.prices)
  }

  "dto" should "be correct" in {
    val state = accountState()
    val dto = state.dto.build()
    dto.getGold should be(state.gold)
    checkPrices(state, dto.getPrices)
  }
}

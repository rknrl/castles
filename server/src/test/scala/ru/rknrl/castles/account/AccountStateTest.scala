package ru.rknrl.castles.account

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.account.objects.items.ItemsTest
import ru.rknrl.castles.account.objects.skills.SkillsTest
import ru.rknrl.castles.account.objects.startLocation.StartLocationTest
import ru.rknrl.castles.account.objects.{BuildingPrototype, Items, Skills, StartLocation}
import ru.rknrl.castles.mock.AccountConfigMock
import ru.rknrl.dto.CommonDTO._

object AccountStateTest {
  def accountState(startLocation: StartLocation = StartLocationTest.startLocation1,
                   skills: Skills = SkillsTest.skills,
                   items: Items = ItemsTest.items,
                   gold: Int = 666,
                   rating: Int = 666,
                   gamesCount: Int = 0) =
    new AccountState(
      startLocation,
      skills,
      items,
      gold,
      rating,
      gamesCount
    )
}

class AccountStateTest extends FlatSpec with Matchers {

  import ru.rknrl.castles.account.AccountStateTest._

  val config = AccountConfigMock.config;

  "swapSlots" should "change startLocation & not change others" in {
    val state = accountState()
    val updated = state.swapSlots(SlotId.SLOT_1, SlotId.SLOT_2)
    updated.startLocation.slots(SlotId.SLOT_1).buildingPrototype should be(state.startLocation.slots(SlotId.SLOT_2).buildingPrototype)
    updated.startLocation.slots(SlotId.SLOT_2).buildingPrototype should be(state.startLocation.slots(SlotId.SLOT_1).buildingPrototype)

    updated.skills should be(state.skills)
    updated.items should be(state.items)
    updated.gold should be(state.gold)
  }

  "buyBuilding" should "throw AssertionError if price < gold" in {
    a[AssertionError] should be thrownBy {
      accountState(gold = 0).buyBuilding(SlotId.SLOT_2, BuildingType.HOUSE, config)
    }
  }

  "buyBuilding" should "change startLocation and gold & not change others" in {
    val state = accountState(gold = 666)
    val updated = state.buyBuilding(SlotId.SLOT_2, BuildingType.HOUSE, config)
    updated.startLocation.slots(SlotId.SLOT_1).buildingPrototype should be(state.startLocation.slots(SlotId.SLOT_1).buildingPrototype)
    updated.startLocation.slots(SlotId.SLOT_2).buildingPrototype.get should be(new BuildingPrototype(BuildingType.HOUSE, BuildingLevel.LEVEL_1))

    updated.gold should be(666 - config.buildingPrices(BuildingLevel.LEVEL_1))

    updated.skills should be(state.skills)
    updated.items should be(state.items)
  }

  "upgradeBuilding" should "throw AssertionError if price < gold" in {
    a[AssertionError] should be thrownBy {
      accountState(gold = 0).upgradeBuilding(SlotId.SLOT_1, config)
    }
  }

  "upgradeBuilding" should "change startLocation and gold & not change others" in {
    val state = accountState(gold = 666)
    val updated = state.upgradeBuilding(SlotId.SLOT_1, config)
    updated.startLocation.slots(SlotId.SLOT_1).buildingPrototype.get.level should be(BuildingLevel.LEVEL_3)

    updated.gold should be(666 - config.buildingPrices(BuildingLevel.LEVEL_3))

    updated.skills should be(state.skills)
    updated.items should be(state.items)
  }

  "removeBuilding" should "change startLocation & not change others" in {
    val state = accountState(startLocation = StartLocationTest.startLocation2)
    val updated = state.removeBuilding(SlotId.SLOT_1)
    updated.startLocation.slots(SlotId.SLOT_1).buildingPrototype should be(None)

    updated.skills should be(state.skills)
    updated.items should be(state.items)
    updated.gold should be(state.gold)
  }

  "upgradeSkill" should "throw AssertionError if price < gold" in {
    a[AssertionError] should be thrownBy {
      val state = accountState(gold = 0)
      state.upgradeSkill(SkillType.ATTACK, config)
    }
  }

  "upgradeSkill" should "change startLocation and gold & not change others" in {
    val state = accountState(gold = 666)
    val updated = state.upgradeSkill(SkillType.ATTACK, config)
    updated.skills.levels(SkillType.ATTACK) should be(SkillLevel.SKILL_LEVEL_2)

    updated.gold should be(666 - config.skillUpgradePrices(state.skills.nextTotalLevel))

    updated.startLocation should be(state.startLocation)
    updated.items should be(state.items)
  }

  "addItem" should "throw AssertionError if price < gold" in {
    a[AssertionError] should be thrownBy {
      accountState(gold = 0).buyItem(ItemType.TORNADO, config)
    }
  }

  "addItem" should "change item and gold & not change others" in {
    val state = accountState(gold = 666)
    val updated = state.buyItem(ItemType.TORNADO, config)

    updated.items.items(ItemType.TORNADO).count should be(state.items.items(ItemType.TORNADO).count + 1)
    updated.gold should be(666 - config.itemPrice)

    updated.startLocation should be(state.startLocation)
    updated.skills should be(state.skills)
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
  }
}

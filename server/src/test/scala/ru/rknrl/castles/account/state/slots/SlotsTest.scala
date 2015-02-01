package ru.rknrl.castles.account.state.slots

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.account.state._
import ru.rknrl.dto.AccountDTO.{SlotDTO, SlotsDTO}
import ru.rknrl.dto.CommonDTO.{BuildingLevel, BuildingType, SlotId}

object SlotsTest {
  private val prototype1 = Some(new BuildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_2))
  val slot1 = SlotId.SLOT_1 → new Slot(SlotId.SLOT_1, prototype1)
  val slot1empty = SlotId.SLOT_1 → new Slot(SlotId.SLOT_1, None)

  private val prototype2 = Some(new BuildingPrototype(BuildingType.HOUSE, BuildingLevel.LEVEL_1))
  val slot2 = SlotId.SLOT_2 → new Slot(SlotId.SLOT_2, prototype2)
  val slot2empty = SlotId.SLOT_2 → new Slot(SlotId.SLOT_2, None)

  val slot3 = SlotId.SLOT_3 → new Slot(SlotId.SLOT_3, None)
  val slot4 = SlotId.SLOT_4 → new Slot(SlotId.SLOT_4, None)
  val slot5 = SlotId.SLOT_5 → new Slot(SlotId.SLOT_5, None)

  val slots = new Slots(Map(slot1, slot2empty, slot3, slot4, slot5))
  val slots2 = new Slots(Map(slot1, slot2, slot3, slot4, slot5))
}

class SlotsTest extends FlatSpec with Matchers {

  import ru.rknrl.castles.account.state.slots.SlotsTest._

  it should "throw AssertionError if not contain all slots" in {
    a[AssertionError] should be thrownBy {
      new Slots(Map(slot1, slot2empty))
    }
  }

  it should "throw AssertionError if all slots are empty" in {
    a[AssertionError] should be thrownBy {
      new Slots(Map(slot1empty, slot2empty, slot3, slot4, slot5))
    }
  }

  "buildingsCount" should "be correct" in {
    slots.buildingsCount should be(1)
  }

  "remove" should "throw AssertionError if buildingCount == 1" in {
    a[AssertionError] should be thrownBy {
      slots.remove(SlotId.SLOT_1)
    }
  }

  def checkSlotsId(slots: Slots) =
    for (id ← SlotId.values()) {
      slots.slots(id).id should be(id)
    }

  "remove" should "change slot & not change other" in {
    val updated = slots2.remove(SlotId.SLOT_1)
    updated.slots(SlotId.SLOT_1).buildingPrototype should be(None)
    updated.slots(SlotId.SLOT_2).buildingPrototype should be(prototype2)

    checkSlotsId(updated)
  }

  "upgrade" should "change slot & not change other" in {
    val updated = slots2.upgrade(SlotId.SLOT_1)
    updated.slots(SlotId.SLOT_1).buildingPrototype.get should be(new BuildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_3))
    updated.slots(SlotId.SLOT_2).buildingPrototype should be(prototype2)

    checkSlotsId(updated)
  }

  "buy" should "change slot & not change other" in {
    val updated = slots2.build(SlotId.SLOT_3, BuildingType.CHURCH)
    updated.slots(SlotId.SLOT_3).buildingPrototype.get should be(new BuildingPrototype(BuildingType.CHURCH, BuildingLevel.LEVEL_1))
    updated.slots(SlotId.SLOT_1).buildingPrototype should be(prototype1)
    updated.slots(SlotId.SLOT_2).buildingPrototype should be(prototype2)

    checkSlotsId(updated)
  }

  "getLevel" should "be correct" in {
    slots.getLevel(SlotId.SLOT_1) should be(BuildingLevel.LEVEL_2)
  }

  "dto" should "be correct" in {
    checkDto(slots, slots.dto)
  }

  def checkDto(slots: Slots, dto: SlotsDTO) = {
    dto.getSlotsCount should be(5)

    getSlots(SlotId.SLOT_1).getId should be(SlotId.SLOT_1)
    getSlots(SlotId.SLOT_1).hasBuildingPrototype should be(true)
    getSlots(SlotId.SLOT_1).getBuildingPrototype.getType should be(prototype1.get.buildingType)
    getSlots(SlotId.SLOT_1).getBuildingPrototype.getLevel should be(prototype1.get.level)

    getSlots(SlotId.SLOT_2).getId should be(SlotId.SLOT_2)
    getSlots(SlotId.SLOT_2).hasBuildingPrototype should be(false)

    getSlots(SlotId.SLOT_3).getId should be(SlotId.SLOT_3)
    getSlots(SlotId.SLOT_3).hasBuildingPrototype should be(false)

    getSlots(SlotId.SLOT_4).getId should be(SlotId.SLOT_4)
    getSlots(SlotId.SLOT_4).hasBuildingPrototype should be(false)

    getSlots(SlotId.SLOT_5).getId should be(SlotId.SLOT_5)
    getSlots(SlotId.SLOT_5).hasBuildingPrototype should be(false)

    def getSlots(slotId: SlotId): SlotDTO = {
      for (i ← 0 until dto.getSlotsCount)
        if (dto.getSlots(i).getId == slotId)
          return dto.getSlots(i)
      throw new IllegalStateException()
    }
  }
}

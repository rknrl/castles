package ru.rknrl.castles.account.state.slots

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.account.state.{BuildingPrototype, Slot}
import ru.rknrl.dto.CommonDTO.{BuildingLevel, BuildingType, SlotId}

class SlotTest extends FlatSpec with Matchers {

  val id1 = SlotId.SLOT_1
  val emptySlot = new Slot(id1, None)

  val id2 = SlotId.SLOT_2
  val slot = new Slot(id2,
    Some(new BuildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_2))
  )

  "remove" should "throw AssertionError on empty slot" in {
    a[AssertionError] should be thrownBy {
      emptySlot.remove
    }
  }

  "remove" should "change prototype to None" in {
    slot.remove.buildingPrototype.isEmpty should be(true)
  }

  "build" should "throw AssertionError on non-empty slot" in {
    a[AssertionError] should be thrownBy {
      slot.build(BuildingType.HOUSE)
    }
  }
  "build" should "change prototype & level & not change id" in {
    val s = emptySlot.build(BuildingType.HOUSE)
    s.id should be(id1)
    s.buildingPrototype.get.buildingType should be(BuildingType.HOUSE)
    s.buildingPrototype.get.level should be(BuildingLevel.LEVEL_1)
  }

  "upgrade" should "throw AssertionError on empty slot" in {
    a[AssertionError] should be thrownBy {
      emptySlot.upgrade
    }
  }

  "upgrade" should "throw Exception on last level slot" in {
    a[Exception] should be thrownBy {
      new Slot(
        id2,
        Some(new BuildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_3))
      ).upgrade
    }
  }

  "upgrade" should "change level & not change prototype and id" in {
    val s = slot.upgrade
    s.id should be(id2)
    s.buildingPrototype.get.buildingType should be(BuildingType.TOWER)
    s.buildingPrototype.get.level should be(BuildingLevel.LEVEL_3)
  }

  "dto" should "be correct with empty slot" in {
    val dto = emptySlot.dto
    dto.getId should be(SlotId.SLOT_1)
    dto.hasBuildingPrototype should be(false)
  }

  "dto" should "be correct with non-empty slot" in {
    val dto = slot.dto
    dto.getId should be(SlotId.SLOT_2)
    dto.hasBuildingPrototype should be(true)
    dto.getBuildingPrototype.getType should be(BuildingType.TOWER)
    dto.getBuildingPrototype.getLevel should be(BuildingLevel.LEVEL_2)
  }
}

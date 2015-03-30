//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account.state

import org.scalatest.{FreeSpec, Matchers}
import ru.rknrl.dto.CommonDTO.BuildingLevel._
import ru.rknrl.dto.CommonDTO.BuildingType._
import ru.rknrl.dto.CommonDTO.SlotId
import ru.rknrl.dto.CommonDTO.SlotId._

class SlotsTest extends FreeSpec with Matchers {
  def slotsBuildings =
    List(
      Slot.empty(SLOT_1),
      Slot.empty(SLOT_2),
      Slot(SLOT_3, BuildingPrototype(HOUSE, LEVEL_1)),
      Slot(SLOT_4, BuildingPrototype(TOWER, LEVEL_1)),
      Slot(SLOT_5, BuildingPrototype(CHURCH, LEVEL_1))
    )

  def slotsMap(slotsBuildings: Iterable[Slot]) =
    slotsBuildings.map(slot ⇒ slot.id → slot).toMap

  def slots = new Slots(slotsMap(slotsBuildings))

  def checkSlotsEquals(newSlots: Slots, without: Option[SlotId]): Unit =
    for ((id, slot) ← slots.slots
         if without.isEmpty || id != without.get;
         newSlot = newSlots(id))
      slot.buildingPrototype shouldBe newSlot.buildingPrototype

  def checkSlotsEquals(newSlots: Slots, without: SlotId): Unit =
    checkSlotsEquals(newSlots, Some(without))

  "all slots are empty" in {
    a[Exception] shouldBe thrownBy {
      new Slots(SlotId.values.map(id ⇒ id → Slot.empty(id)).toMap)
    }
  }

  "не все слоты" in {
    a[Exception] shouldBe thrownBy {
      new Slots(Map(SLOT_1 → Slot(SLOT_1, BuildingPrototype(TOWER, LEVEL_1))))
      new Slots(Map(SLOT_2 → Slot(SLOT_2, BuildingPrototype(TOWER, LEVEL_1))))
    }
  }

  "buildingsCount" in {
    slots.buildingsCount shouldBe 3
  }

  "set" in {
    val newSlots = slots.set(SLOT_3, BuildingPrototype(TOWER, LEVEL_3))
    newSlots(SLOT_3).buildingPrototype.get shouldBe BuildingPrototype(TOWER, LEVEL_3)
    checkSlotsEquals(newSlots, SLOT_3)
  }

  "remove" - {
    "remove last building" in {

      a[Exception] shouldBe thrownBy {

        new Slots(slotsMap(List(
          Slot.empty(SLOT_1),
          Slot.empty(SLOT_2),
          Slot.empty(SLOT_3),
          Slot.empty(SLOT_4),
          Slot(SLOT_5, BuildingPrototype(CHURCH, LEVEL_3))
        )))
          .remove(SLOT_5)
      }
    }

    "normal remove" in {
      val newSlots = slots.remove(SLOT_5)
      newSlots(SLOT_5).buildingPrototype shouldBe empty
      checkSlotsEquals(newSlots, SLOT_5)
    }
  }

  "build" in {
    val newSlots = slots.build(SLOT_1, TOWER)
    newSlots(SLOT_1).buildingPrototype.get shouldBe BuildingPrototype(TOWER, LEVEL_1)
    checkSlotsEquals(newSlots, SLOT_1)
  }

  "upgrade" in {
    val newSlots = slots.upgrade(SLOT_3)
    newSlots(SLOT_3).buildingPrototype.get shouldBe BuildingPrototype(HOUSE, LEVEL_2)
    checkSlotsEquals(newSlots, SLOT_3)
  }

  "dto" in {
    val dto = slots.dto

    def get(slotId: SlotId) =
      dto.find(_.getId == slotId).get

    dto should have size 5

    get(SLOT_1).hasBuildingPrototype shouldBe false
    get(SLOT_2).hasBuildingPrototype shouldBe false

    get(SLOT_3).getBuildingPrototype.getType shouldBe HOUSE
    get(SLOT_3).getBuildingPrototype.getLevel shouldBe LEVEL_1

    get(SLOT_4).getBuildingPrototype.getType shouldBe TOWER
    get(SLOT_4).getBuildingPrototype.getLevel shouldBe LEVEL_1

    get(SLOT_5).getBuildingPrototype.getType shouldBe CHURCH
    get(SLOT_5).getBuildingPrototype.getLevel shouldBe LEVEL_1
  }

  "parse from dto" in {
    checkSlotsEquals(Slots(slots.dto), None)
  }
}

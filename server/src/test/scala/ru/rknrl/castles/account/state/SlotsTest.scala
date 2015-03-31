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
}

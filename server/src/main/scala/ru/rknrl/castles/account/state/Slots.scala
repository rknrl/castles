//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account.state

import ru.rknrl.Assertion
import ru.rknrl.dto.AccountDTO.SlotDTO
import ru.rknrl.dto.CommonDTO._

class Slots(val slots: Map[SlotId, SlotDTO]) {
  for (slotId ← SlotId.values) Assertion.check(slots.contains(slotId))
  Assertion.check(buildingsCount > 0)

  def apply(id: SlotId) = slots(id)

  def updated(id: SlotId, slot: SlotDTO) =
    new Slots(slots.updated(id, slot))

  def buildingsCount =
    slots.values.count(_.hasBuildingPrototype)

  def dto = slots.values
}

object Slots {
  val positions = Map(
    SlotId.SLOT_1 → IJ(Slot1Pos.SLOT_1_X_VALUE, Slot1Pos.SLOT_1_Y_VALUE),
    SlotId.SLOT_2 → IJ(Slot2Pos.SLOT_2_X_VALUE, Slot2Pos.SLOT_2_Y_VALUE),
    SlotId.SLOT_3 → IJ(Slot3Pos.SLOT_3_X_VALUE, Slot3Pos.SLOT_3_Y_VALUE),
    SlotId.SLOT_4 → IJ(Slot4Pos.SLOT_4_X_VALUE, Slot4Pos.SLOT_4_Y_VALUE),
    SlotId.SLOT_5 → IJ(Slot5Pos.SLOT_5_X_VALUE, Slot5Pos.SLOT_5_Y_VALUE)
  )

  def apply(dto: Iterable[SlotDTO]) = {
    val slots = for (slotDto ← dto) yield slotDto.getId → slotDto
    new Slots(slots.toMap)
  }
}
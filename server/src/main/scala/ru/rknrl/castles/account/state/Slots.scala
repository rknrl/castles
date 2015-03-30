//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account.state

import ru.rknrl.dto.AccountDTO.SlotDTO
import ru.rknrl.dto.CommonDTO._

class Slots(val slots: Map[SlotId, Slot]) {
  for (slotId ← SlotId.values()) assert(slots.contains(slotId))
  assert(buildingsCount > 0)

  def apply(id: SlotId) = slots(id)

  def buildingsCount =
    slots.values.count(_.buildingPrototype.isDefined)

  def set(id: SlotId, buildingPrototype: BuildingPrototype) =
    update(id, slots(id).set(buildingPrototype))

  def remove(id: SlotId) = {
    assert(buildingsCount > 1)
    update(id, slots(id).remove)
  }

  def build(id: SlotId, buildingType: BuildingType) =
    update(id, slots(id).build(buildingType))

  def upgrade(id: SlotId) =
    update(id, slots(id).upgrade)

  private def update(id: SlotId, newSlot: Slot) =
    new Slots(slots.updated(id, newSlot))

  def dto = slots.values.map(_.dto)
}

object Slots {
  val positions = Map(
    SlotId.SLOT_1 → IJ(Slot1Pos.SLOT_1_X_VALUE, Slot1Pos.SLOT_1_Y_VALUE),
    SlotId.SLOT_2 → IJ(Slot2Pos.SLOT_2_X_VALUE, Slot2Pos.SLOT_2_Y_VALUE),
    SlotId.SLOT_3 → IJ(Slot3Pos.SLOT_3_X_VALUE, Slot3Pos.SLOT_3_Y_VALUE),
    SlotId.SLOT_4 → IJ(Slot4Pos.SLOT_4_X_VALUE, Slot4Pos.SLOT_4_Y_VALUE),
    SlotId.SLOT_5 → IJ(Slot5Pos.SLOT_5_X_VALUE, Slot5Pos.SLOT_5_Y_VALUE)
  )
  val left = Math.abs(-2)
  val right = 2
  val top = Math.abs(-1)
  val bottom = 0

  def apply(dto: Iterable[SlotDTO]) = {
    val slots = for (slotDto ← dto) yield slotDto.getId → Slot(slotDto)
    new Slots(slots.toMap)
  }
}
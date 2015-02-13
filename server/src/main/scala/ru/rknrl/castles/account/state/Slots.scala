//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account.state

import ru.rknrl.castles.game.points.Point
import ru.rknrl.dto.AccountDTO.SlotDTO
import ru.rknrl.dto.CommonDTO._
import ru.rknrl.dto.GameDTO.CellSize

class Slots(val slots: Map[SlotId, Slot]) {
  for (slotId ← SlotId.values()) assert(slots.contains(slotId))
  assert(buildingsCount > 0)

  def buildingsCount = {
    var count = 0
    for ((id, slot) ← slots) {
      if (slot.buildingPrototype.isDefined) count += 1
    }
    count
  }

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

  def getLevel(id: SlotId) = slots(id).buildingPrototype.get.level

  private def update(id: SlotId, newSlot: Slot) =
    new Slots(slots.updated(id, newSlot))

  def dto = for ((id, slot) ← slots) yield slot.dto
}

class IJ(val i: Int, val j: Int) {
  override def equals(obj: scala.Any): Boolean = obj match {
    case that: IJ ⇒ this.i == that.i && this.j == that.j
    case _ ⇒ false
  }

  def up = new IJ(i, j - 1)

  def upLeft = new IJ(i - 1, j - 1)

  def upRight = new IJ(i + 1, j - 1)

  def left = new IJ(i - 1, j)

  def right = new IJ(i + 1, j)

  def down = new IJ(i, j + 1)

  def downLeft = new IJ(i - 1, j + 1)

  def downRight = new IJ(i + 1, j + 1)

  def toXY = new Point((i + 0.5) * CellSize.SIZE_VALUE, (j + 0.5) * CellSize.SIZE_VALUE)

  def near(that: IJ) = up == that || upLeft == that || upRight == that ||
    this == that || left == that || right == that ||
    down == that || downLeft == that || downRight == that
}

object Slots {
  val positions = Map(
    SlotId.SLOT_1 → new IJ(Slot1Pos.SLOT_1_X_VALUE, Slot1Pos.SLOT_1_Y_VALUE),
    SlotId.SLOT_2 → new IJ(Slot2Pos.SLOT_2_X_VALUE, Slot2Pos.SLOT_2_Y_VALUE),
    SlotId.SLOT_3 → new IJ(Slot3Pos.SLOT_3_X_VALUE, Slot3Pos.SLOT_3_Y_VALUE),
    SlotId.SLOT_4 → new IJ(Slot4Pos.SLOT_4_X_VALUE, Slot4Pos.SLOT_4_Y_VALUE),
    SlotId.SLOT_5 → new IJ(Slot5Pos.SLOT_5_X_VALUE, Slot5Pos.SLOT_5_Y_VALUE)
  )
  val left = Math.abs(-2)
  val right = 2
  val top = Math.abs(-1)
  val bottom = 0

  def fromDto(dto: Iterable[SlotDTO]) = {
    val slots = for (slotDto ← dto) yield slotDto.getId → Slot.fromDto(slotDto)

    new Slots(slots.toMap)
  }
}
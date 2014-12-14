package ru.rknrl.castles.account.objects

import ru.rknrl.dto.AccountDTO.StartLocationDTO
import ru.rknrl.dto.CommonDTO.{BuildingType, SlotId}
import ru.rknrl.dto.GameDTO.CellSize
import ru.rknrl.utils.Point

import scala.collection.JavaConverters._

class StartLocation(val slots: Map[SlotId, StartLocationSlot]) {
  for (slotId ← SlotId.values()) assert(slots.contains(slotId))
  assert(buildingsCount > 0)

  def swap(id1: SlotId, id2: SlotId) = {
    val slot1 = new StartLocationSlot(id2, slots(id1).buildingPrototype)
    val slot2 = new StartLocationSlot(id1, slots(id2).buildingPrototype)
    new StartLocation(slots.updated(id1, slot2).updated(id2, slot1))
  }

  def buildingsCount = {
    var count = 0
    for ((id, slot) ← slots) {
      if (slot.buildingPrototype.isDefined) count += 1
    }
    count
  }

  def remove(id: SlotId) = {
    assert(buildingsCount > 1)
    update(id, slots(id).remove)
  }

  def buy(id: SlotId, buildingType: BuildingType) =
    update(id, slots(id).build(buildingType))

  def upgrade(id: SlotId) =
    update(id, slots(id).upgrade)

  def getLevel(id: SlotId) = slots(id).buildingPrototype.get.level

  private def update(id: SlotId, newSlot: StartLocationSlot) =
    new StartLocation(slots.updated(id, newSlot))

  private def slotsDto = for ((id, slot) ← slots) yield slot.dto

  def dto = StartLocationDTO.newBuilder().addAllSlots(slotsDto.asJava).build()
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

object StartLocation {
  val positions = Map(
    SlotId.SLOT_1 → new IJ(-2, 0),
    SlotId.SLOT_2 → new IJ(-1, -1),
    SlotId.SLOT_3 → new IJ(0, 0),
    SlotId.SLOT_4 → new IJ(1, -1),
    SlotId.SLOT_5 → new IJ(2, 0)
  )
  val left = Math.abs(-2)
  val right = 2
  val top = Math.abs(-1)
  val bottom = 0
}
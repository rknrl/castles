package ru.rknrl.utils

import ru.rknrl.castles.game.objects.buildings.BuildingId
import ru.rknrl.castles.game.objects.units.UnitId

class IdIterator {
  final val max = Int.MaxValue
  private var id = 0

  protected def nextInt: Int = {
    id += 1
    if (id > max) id = 0
    id
  }
}

class BuildingIdIterator extends IdIterator {
  def next = new BuildingId(nextInt)
}

class UnitIdIterator extends IdIterator {
  def next = new UnitId(nextInt)
}

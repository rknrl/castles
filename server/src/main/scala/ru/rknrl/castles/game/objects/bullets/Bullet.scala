package ru.rknrl.castles.game.objects.bullets

import ru.rknrl.castles.game.objects.buildings.Building
import ru.rknrl.castles.game.objects.units.GameUnit
import ru.rknrl.utils.PeriodObject
import ru.rknrl.dto.GameDTO.BulletDTO

class Bullet(val building: Building,
             val unit: GameUnit,
             val startTime: Long,
             val duration: Long) extends PeriodObject[BulletDTO] {

  def dto(time: Long) = {
    timeAssert(time)

    BulletDTO.newBuilder()
      .setBuildingId(building.id.dto)
      .setUnitId(unit.id.dto)
      .setDuration(duration.toInt)
      .build
  }
}

package ru.rknrl.castles.game.state.bullets

import ru.rknrl.castles.game.state.buildings.Building
import ru.rknrl.castles.game.state.units.GameUnit
import ru.rknrl.dto.GameDTO.BulletDTO
import ru.rknrl.utils.PeriodObject

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
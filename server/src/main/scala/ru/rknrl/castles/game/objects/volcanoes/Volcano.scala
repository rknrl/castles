package ru.rknrl.castles.game.objects.volcanoes

import ru.rknrl.castles.game.objects.players.PlayerId
import ru.rknrl.utils.{PeriodObject, Point}
import ru.rknrl.dto.GameDTO.VolcanoDTO

class Volcano(val playerId: PlayerId,
              val x: Double,
              val y: Double,
              val startTime: Long,
              val duration: Long) extends PeriodObject[VolcanoDTO] {

  val pos = new Point(x, y)

  def dto(time: Long) = {
    timeAssert(time)

    VolcanoDTO.newBuilder()
      .setX(x.toFloat)
      .setY(y.toFloat)
      .setMillisTillEnd(millisTillEnd(time))
      .build()
  }
}


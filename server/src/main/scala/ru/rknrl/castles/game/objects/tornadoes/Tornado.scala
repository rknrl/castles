package ru.rknrl.castles.game.objects.tornadoes

import ru.rknrl.castles.game.objects.players.PlayerId
import ru.rknrl.utils.{PeriodObject, Points}
import ru.rknrl.dto.GameDTO.TornadoDTO

import scala.collection.JavaConverters._

class Tornado(val playerId: PlayerId,
              val points: Points,
              val startTime: Long,
              val duration: Long,
              val speed: Double) extends PeriodObject[TornadoDTO] {

  def getPos(time: Long) = points.getPos(millisFromsStart(time), speed)

  def dto(time: Long) = {
    timeAssert(time)

    TornadoDTO.newBuilder()
      .addAllPoints(points.dto.asJava)
      .setSpeed(speed.toFloat)
      .setMillisFromStart(millisFromsStart(time))
      .setMillisTillEnd(millisTillEnd(time))
      .build()
  }
}

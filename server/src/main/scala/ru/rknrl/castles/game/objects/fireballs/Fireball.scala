package ru.rknrl.castles.game.objects.fireballs

import ru.rknrl.castles.game.objects.players.PlayerId
import ru.rknrl.dto.GameDTO.FireballDTO
import ru.rknrl.utils.{PeriodObject, Point}

class Fireball(val playerId: PlayerId,
               val pos: Point,
               val duration: Long,
               val startTime: Long) extends PeriodObject[FireballDTO] {

  def dto(time: Long) = FireballDTO.newBuilder()
    .setX(pos.x.toFloat)
    .setY(pos.y.toFloat)
    .setMillisTillSplash(millisTillEnd(time))
    .build()
}

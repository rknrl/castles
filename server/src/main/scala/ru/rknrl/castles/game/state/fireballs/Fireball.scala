package ru.rknrl.castles.game.state.fireballs

import ru.rknrl.castles.game.state.players.PlayerId
import ru.rknrl.dto.GameDTO.FireballDTO
import ru.rknrl.utils.{PeriodObject, Point}

class Fireball(val playerId: PlayerId,
               val pos: Point,
               val duration: Long,
               val startTime: Long) extends PeriodObject[FireballDTO] {

  def dto(time: Long) = FireballDTO.newBuilder()
    .setPos(pos.dto)
    .setMillisTillSplash(millisTillEnd(time))
    .build()
}

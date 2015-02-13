package ru.rknrl.castles.game.state.volcanoes

import ru.rknrl.castles.game.state.players.PlayerId
import ru.rknrl.castles.game.points.Point
import ru.rknrl.dto.GameDTO.VolcanoDTO
import ru.rknrl.utils.PeriodObject

class Volcano(val playerId: PlayerId,
              val pos: Point,
              val startTime: Long,
              val duration: Long) extends PeriodObject[VolcanoDTO] {

  def dto(time: Long) = {
    timeAssert(time)

    VolcanoDTO.newBuilder()
      .setPos(pos.dto)
      .setMillisTillEnd(millisTillEnd(time))
      .build()
  }
}


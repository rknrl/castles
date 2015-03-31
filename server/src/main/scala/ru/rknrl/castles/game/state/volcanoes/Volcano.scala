//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state.volcanoes

import ru.rknrl.castles.game.state.players.PlayerId
import ru.rknrl.castles.game.points.Point
import ru.rknrl.dto.GameDTO.{PlayerIdDTO, VolcanoDTO}
import ru.rknrl.utils.PeriodObject

class Volcano(val playerId: PlayerIdDTO,
              val pos: Point,
              val startTime: Long,
              val duration: Long) extends PeriodObject[VolcanoDTO] {

  def dto(time: Long) =
    VolcanoDTO.newBuilder
      .setPos(pos.dto)
      .setMillisTillEnd(millisTillEnd(time))
      .build
}


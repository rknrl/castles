//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state.fireballs

import ru.rknrl.castles.game.state.players.PlayerId
import ru.rknrl.castles.game.points.Point
import ru.rknrl.dto.GameDTO.{PlayerIdDTO, FireballDTO}
import ru.rknrl.utils.PeriodObject

class Fireball(val playerId: PlayerIdDTO,
               val pos: Point,
               val duration: Long,
               val startTime: Long) extends PeriodObject[FireballDTO] {

  def dto(time: Long) = FireballDTO.newBuilder
    .setPos(pos.dto)
    .setMillisTillSplash(millisTillEnd(time))
    .build
}

//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state.tornadoes

import ru.rknrl.castles.game.state.players.PlayerId
import ru.rknrl.castles.game.points.Points
import ru.rknrl.dto.GameDTO.{PlayerIdDTO, TornadoDTO}
import ru.rknrl.utils.PeriodObject

import scala.collection.JavaConverters._

class Tornado(val playerId: PlayerIdDTO,
              val points: Points,
              val startTime: Long,
              val duration: Long,
              val speed: Double) extends PeriodObject[TornadoDTO] {

  def pos(time: Long) = points.getPos(millisFromsStart(time), speed)

  def dto(time: Long) =
    TornadoDTO.newBuilder
      .addAllPoints(points.dto.asJava)
      .setSpeed(speed.toFloat)
      .setMillisFromStart(millisFromsStart(time))
      .setMillisTillEnd(millisTillEnd(time))
      .build
}

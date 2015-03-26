//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import ru.rknrl.castles.game.GameConfig
import ru.rknrl.castles.game.points.Point
import ru.rknrl.castles.game.state.buildings.{BuildingId, Buildings}
import ru.rknrl.castles.game.state.players.{PlayerId, PlayerStates}
import ru.rknrl.castles.game.state.units.GameUnit

object Assistance {
  def `casts→units`(casts: Map[PlayerId, BuildingId],
                    buildings: Buildings,
                    config: GameConfig,
                    playerStates: PlayerStates,
                    unitIdIterator: UnitIdIterator,
                    assistancePositions: Map[PlayerId, Point],
                    time: Long) =
    for ((playerId, buildingId) ← casts) yield {
      val building = buildings(buildingId)

      // assert(building.owner.get == playerId)
      // здание может быть захвачено противником до каста, в этом случае все равно отправляем отряд

      val startPos = assistancePositions(playerId)
      val endPos = building.pos
      val prototype = config.assistanceBuildingPrototype
      val speed = config.unitSpeed(prototype, playerStates(playerId), strengthened = false)
      val count = config.assistanceCount(building, playerStates(playerId))
      new GameUnit(unitIdIterator.next, prototype, count, startPos, endPos, time, speed, buildingId, playerId, false)
    }
}

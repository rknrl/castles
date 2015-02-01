package ru.rknrl.castles.game.state

import ru.rknrl.castles.game.state.buildings.{BuildingId, Buildings}
import ru.rknrl.castles.game.state.players.{PlayerId, PlayerStates}
import ru.rknrl.castles.game.state.units.GameUnit
import ru.rknrl.castles.game.GameConfig
import ru.rknrl.utils.Point

object Assistance {
  def `casts→units`(casts: Map[PlayerId, BuildingId],
                    buildings: Buildings,
                    config: GameConfig,
                    playerStates: PlayerStates,
                    unitIdIterator: UnitIdIterator,
                    time: Long) =
    for ((playerId, buildingId) ← casts) yield {
      val building = buildings(buildingId)

      // assert(building.owner.get == playerId)
      // здание может быть захвачено противником до каста, в этом случае все равно отправляем отряд

      val startPos = new Point(0, 0)
      val endPos = building.pos
      val prototype = config.assistanceBuildingPrototype
      val speed = config.getUnitSpeed(prototype, playerStates(playerId), strengthened = false)
      new GameUnit(unitIdIterator.next, prototype, config.assistanceCount(playerStates(playerId)), startPos, endPos, time, speed, buildingId, playerId, false)
    }
}

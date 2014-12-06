package ru.rknrl.castles.game.objects

import ru.rknrl.castles.game.objects.buildings.{BuildingId, Buildings}
import ru.rknrl.castles.game.objects.players.{PlayerStates, PlayerId, Players}
import ru.rknrl.castles.game.objects.units.GameUnit
import ru.rknrl.utils.{Point, UnitIdIterator}
import ru.rknrl.castles.game.{GameConfig, GameState}

object Assistance {
  def `casts→units`(casts: Map[PlayerId, BuildingId],
                    buildings: Buildings,
                    config: GameConfig,
                    playerStates: PlayerStates,
                    unitIdIterator: UnitIdIterator,
                    time: Long) =
    for ((playerId, buildingId) ← casts) yield {
      val building = buildings(buildingId)
      assert(building.owner.get == playerId)
      val startPos = new Point(0, 0)
      val endPos = new Point(building.x, building.y)
      val prototype = config.assistanceBuildingPrototype
      val speed = config.getUnitSpeed(prototype, playerStates(playerId), strengthened = false)
      new GameUnit(unitIdIterator.next, prototype, config.assistanceCount(playerStates(playerId)), startPos, endPos, time, speed, buildingId, playerId, false)
    }
}

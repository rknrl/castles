//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import ru.rknrl.castles.game.GameConfig
import ru.rknrl.castles.game.state.buildings.{BuildingId, Buildings}
import ru.rknrl.castles.game.state.players.{PlayerId, PlayerStates}
import ru.rknrl.castles.game.state.units.GameUnit
import ru.rknrl.dto.GameDTO.{PlayerIdDTO, MoveDTO}

import scala.collection.JavaConverters._

object Moving {

  case class ExitUnit(playerId: PlayerIdDTO, fromBuildingId: BuildingId, toBuildingId: BuildingId)

  case class EnterUnit(unit: GameUnit)

  def `moveActions→exitUnits`(moveActions: Map[PlayerIdDTO, MoveDTO], buildings: Buildings, config: GameConfig) =
    for ((playerId, moveDto) ← moveActions;
         fromBuildingsDto = moveDto.getFromBuildingsList.asScala.toList;
         fromBuildingDto ← fromBuildingsDto;
         fromBuildingId = BuildingId(fromBuildingDto.getId);
         fromBuilding = buildings(fromBuildingId)
         if config.unitsToExit(fromBuilding.floorPopulation) >= 1;
         toBuildingId = BuildingId(moveDto.getToBuilding.getId)
         if fromBuildingId != toBuildingId
         if fromBuilding.owner.get == playerId
    ) yield
    ExitUnit(playerId, fromBuildingId, toBuildingId)


  def `exitUnit→units`(exitUnits: Iterable[ExitUnit], buildings: Buildings, config: GameConfig, unitIdIterator: UnitIdIterator, playerStates: PlayerStates, time: Long) =
    for (exitUnit ← exitUnits) yield {
      val fromBuilding = buildings(exitUnit.fromBuildingId)
      val toBuilding = buildings(exitUnit.toBuildingId)

      val count = config.unitsToExit(fromBuilding.floorPopulation)

      val unitId = unitIdIterator.next

      val starPos = fromBuilding.pos
      val endPos = toBuilding.pos

      val buildingPrototype = fromBuilding.prototype
      val playerState = playerStates(fromBuilding.owner.get)
      val speed = config.unitSpeed(buildingPrototype, playerState, fromBuilding.strengthened)

      new GameUnit(unitId, buildingPrototype, count, starPos, endPos, time, speed, exitUnit.toBuildingId, exitUnit.playerId, fromBuilding.strengthened)
    }

  def `units→enterUnit`(units: Iterable[GameUnit], time: Long) =
    for (unit ← units if unit.pos(time) == unit.endPos) yield EnterUnit(unit)
}

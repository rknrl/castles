package ru.rknrl.castles.game.objects

import ru.rknrl.castles.game.GameConfig
import ru.rknrl.castles.game.objects.buildings.{BuildingId, Buildings}
import ru.rknrl.castles.game.objects.players.{PlayerId, PlayerStates}
import ru.rknrl.castles.game.objects.units.GameUnit
import ru.rknrl.castles.rmi.RemoveUnitMsg
import ru.rknrl.dto.GameDTO.MoveDTO
import ru.rknrl.utils.UnitIdIterator

import scala.collection.JavaConverters._

object Moving {

  case class ExitUnit(playerId: PlayerId, fromBuildingId: BuildingId, toBuildingId: BuildingId)

  case class EnterUnit(unit: GameUnit)

  def `moveActions→exitUnits`(moveActions: Map[PlayerId, MoveDTO], buildings: Buildings, config: GameConfig) =
    for ((playerId, moveDto) ← moveActions;
         fromBuildingsDto = moveDto.getFromBuildingsList.asScala.toList;
         fromBuildingDto ← fromBuildingsDto;
         fromBuildingId = new BuildingId(fromBuildingDto.getId);
         fromBuilding = buildings(fromBuildingId)
         if config.unitsToExit(fromBuilding.floorPopulation) >= 1
    ) yield {
      val toBuildingId = new BuildingId(moveDto.getToBuilding.getId)

      assert(fromBuildingId != toBuildingId)

      assert(fromBuilding.owner.get == playerId)

      ExitUnit(playerId, fromBuildingId, toBuildingId)
    }

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
      val speed = config.getUnitSpeed(buildingPrototype, playerState, fromBuilding.strengthened)

      new GameUnit(unitId, buildingPrototype, count, starPos, endPos, time, speed, exitUnit.toBuildingId, exitUnit.playerId, fromBuilding.strengthened)
    }

  def `units→enterUnit`(units: Iterable[GameUnit], time: Long) =
    for (unit ← units if unit.getPos(time) == unit.endPos) yield EnterUnit(unit)

  def `enterUnit→removeUnitMsg`(enterUnits: Iterable[EnterUnit]) =
    for (enterUnit ← enterUnits) yield RemoveUnitMsg(enterUnit.unit.id.dto)
}

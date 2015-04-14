//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import ru.rknrl.dto.{MoveDTO, PlayerId}

object Moving {
  /**
   * Сколько юнитов выйдут из здания
   */
  def unitsToExit(buildingPopulation: Double): Int =
    Math.floor(buildingPopulation * 0.5).toInt

  def moveActionToExitUnit(move: Move, unitIdIterator: UnitIdIterator, time: Long) = {
    val distance = move.fromBuilding.pos.distance(move.toBuilding.pos)
    val speed = move.fromBuilding.stat.speed

    new GameUnit(
      id = unitIdIterator.next,
      fromBuilding = move.fromBuilding,
      toBuilding = move.toBuilding,
      count = unitsToExit(move.fromBuilding.count),
      startTime = time,
      duration = (distance / speed).toLong
    )
  }

  case class Move(playerId: PlayerId, fromBuilding: Building, toBuilding: Building)

  def convert(moveActions: Map[PlayerId, MoveDTO], buildings: Iterable[Building]) =
    for ((playerId, move) ← moveActions;
         fromBuildingId ← move.fromBuildings)
      yield Move(
        playerId,
        fromBuilding = buildings.find(_.id == fromBuildingId).get,
        toBuilding = buildings.find(_.id == move.toBuilding).get
      )

  def moveActionsToExitUnits(moveActions: Map[PlayerId, MoveDTO], buildings: Iterable[Building], unitIdIterator: UnitIdIterator, time: Long) =
    convert(moveActions, buildings)
      .filter(m ⇒ m.fromBuilding.id != m.toBuilding.id)
      .filter(m ⇒ m.fromBuilding.owner.isDefined && m.fromBuilding.owner.get.id == m.playerId)
      .filter(m ⇒ m.fromBuilding.count >= 2)
      .map(moveActionToExitUnit(_, unitIdIterator, time))
}

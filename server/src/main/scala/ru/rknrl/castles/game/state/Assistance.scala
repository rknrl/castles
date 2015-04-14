//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import ru.rknrl.castles.game.GameConfig
import ru.rknrl.dto.{BuildingId, PlayerId}

object Assistance {
  def castToUnit(cast: (Player, BuildingId),
                 buildings: Iterable[Building],
                 config: GameConfig,
                 churchesProportion: ChurchesProportion,
                 unitIdIterator: UnitIdIterator,
                 assistancePositions: Map[PlayerId, Point],
                 time: Long) = {

    val player = cast._1
    val buildingId = cast._2
    val toBuilding = buildings.find(_.id == buildingId).get
    val prototype = config.assistance.buildingPrototype

    val fromBuilding = new Building(
      id = new BuildingId(-1),
      buildingPrototype = prototype,
      pos = assistancePositions(player.id),
      owner = Some(player),
      count = 0,
      buildingStat = config.units(prototype),
      strengthening = None,
      lastShootTime = 0
    )

    val distance = fromBuilding.pos.distance(toBuilding.pos)
    val speed = fromBuilding.stat.speed

    new GameUnit(
      id = unitIdIterator.next,
      fromBuilding = fromBuilding,
      toBuilding = toBuilding,
      count = config.assistanceCount(toBuilding, churchesProportion(player.id)),
      startTime = time,
      duration = (distance / speed).toLong
    )
  }
}

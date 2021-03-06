//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import protos.BuildingType.CHURCH
import protos.PlayerId
import ru.rknrl.castles.game.GameConfig

class ChurchesProportion(val map: Map[PlayerId, Double]) {

  def apply(playerId: Option[PlayerId]) =
    if (playerId.isDefined) Some(map(playerId.get)) else None

  def apply(id: PlayerId) = map(id)

}

object ChurchesProportion {
  private def totalChurchesPopulation(buildings: Iterable[Building], config: GameConfig) = {
    var total = 0.0
    for (b ← buildings if b.buildingPrototype.buildingType == CHURCH)
      total += config.maxCount(b)
    total
  }

  private def playerChurchesPopulation(buildings: Iterable[Building], playerId: PlayerId) = {
    var count = 0.0
    for (b ← buildings
         if b.buildingPrototype.buildingType == CHURCH && b.owner.isDefined && b.owner.get.id == playerId)
      count += b.count
    count
  }

  def getChurchesProportion(buildings: Iterable[Building], players: Map[PlayerId, Player], config: GameConfig) = {
    val total = totalChurchesPopulation(buildings, config)
    val newStates = for ((playerId, _) ← players) yield {
      val proportion = if (total == 0) 0 else playerChurchesPopulation(buildings, playerId) / total
      playerId → proportion
    }
    new ChurchesProportion(newStates)
  }

}
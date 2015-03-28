//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state.players

import ru.rknrl.castles.game.state.Stat
import ru.rknrl.castles.game.state.buildings.{Building, BuildingId}
import ru.rknrl.dto.CommonDTO.BuildingType

class PlayerState(val stat: Stat,
                  val churchesProportion: Double) {
  def setChurchesProportion(value: Double) =
    new PlayerState(stat, value)
}

class PlayerStates(val states: Map[PlayerId, PlayerState]) {

  def apply(playerId: Option[PlayerId]) =
    if (playerId.isDefined) Some(states(playerId.get)) else None

  def apply(id: PlayerId) = states(id)

  private def totalChurchesPopulaton(buildings: Map[BuildingId, Building]) = {
    var total = 0.0
    for ((id, b) ← buildings if b.prototype.buildingType == BuildingType.CHURCH)
      total += b.population
    total
  }

  private def playerChurchesPopulation(buildings: Map[BuildingId, Building], playerId: PlayerId) = {
    var population = 0.0
    for ((id, b) ← buildings
         if b.prototype.buildingType == BuildingType.CHURCH && b.owner.isDefined && b.owner.get == playerId) {
      population += b.population
    }
    population
  }

  def updateChurchesProportion(buildings: Map[BuildingId, Building]) = {
    var newStates = states
    val total = totalChurchesPopulaton(buildings)
    for ((playerId, state) ← states) {
      val churchesPopulation = playerChurchesPopulation(buildings, playerId)
      newStates = newStates.updated(playerId, state.setChurchesProportion(churchesPopulation / total))
    }
    new PlayerStates(newStates)
  }

  def setChurchesProportion(id: PlayerId, value: Double) =
    new PlayerStates(states.updated(id, states(id).setChurchesProportion(value)))
}

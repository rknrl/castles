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
import ru.rknrl.dto.CommonDTO.BuildingType._
import ru.rknrl.dto.GameDTO.PlayerIdDTO

class PlayerState(val stat: Stat,
                  val churchesProportion: Double) {
  def setChurchesProportion(value: Double) =
    new PlayerState(stat, value)
}

class PlayerStates(states: Map[PlayerIdDTO, PlayerState]) {

  def apply(playerId: Option[PlayerIdDTO]) =
    if (playerId.isDefined) Some(states(playerId.get)) else None

  def apply(id: PlayerIdDTO) = states(id)

  private def totalChurchesPopulation(buildings: Map[BuildingId, Building]) = {
    var total = 0.0
    for ((id, b) ← buildings if b.prototype.getType == CHURCH)
      total += b.population
    total
  }

  private def playerChurchesPopulation(buildings: Map[BuildingId, Building], playerId: PlayerIdDTO) = {
    var population = 0.0
    for ((id, b) ← buildings
         if b.prototype.getType == CHURCH && b.owner == Some(playerId))
      population += b.population
    population
  }

  def updateChurchesProportion(buildings: Map[BuildingId, Building]) = {
    var newStates = states
    val total = totalChurchesPopulation(buildings)
    for ((playerId, state) ← states) {
      val churchesPopulation = playerChurchesPopulation(buildings, playerId)
      newStates = newStates.updated(playerId, state.setChurchesProportion(churchesPopulation / total))
    }
    new PlayerStates(newStates)
  }
}

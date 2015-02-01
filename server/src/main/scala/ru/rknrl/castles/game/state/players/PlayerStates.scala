package ru.rknrl.castles.game.state.players

import ru.rknrl.castles.game.state.Stat
import ru.rknrl.castles.game.state.buildings.{Building, BuildingId}
import ru.rknrl.dto.CommonDTO.BuildingType

class PlayerState(val stat: Stat,
                  val churchesPopulation: Double) {
  def setChurchesPopulation(value: Double) =
    new PlayerState(stat, value)
}

class PlayerStates(val states: Map[PlayerId, PlayerState]) {

  private def getChurchesPopulation(buildings: Map[BuildingId, Building], playerId: PlayerId) = {
    var population = 0.0
    for ((id, b) ← buildings
         if b.prototype.buildingType == BuildingType.CHURCH && b.owner.isDefined && b.owner.get == playerId) {
      population += b.population
    }
    population
  }

  def updateChurchesPopulation(buildings: Map[BuildingId, Building]) = {
    var newStates = states
    for ((playerId, state) ← states) {
      val churchesPopulation = getChurchesPopulation(buildings, playerId)
      newStates = newStates.updated(playerId, state.setChurchesPopulation(churchesPopulation))
    }
    new PlayerStates(newStates)
  }

  def apply(id: PlayerId) = states(id)

  def setChurchesPopulation(id: PlayerId, value: Double) =
    new PlayerStates(states.updated(id, states(id).setChurchesPopulation(value)))
}

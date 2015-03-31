//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state.buildings

import ru.rknrl.Assertion
import ru.rknrl.castles.game._
import ru.rknrl.castles.game.state.Moving.{EnterUnit, ExitUnit}
import ru.rknrl.castles.game.state.bullets.Bullet
import ru.rknrl.castles.game.state.fireballs.{Fireball, Fireballs}
import ru.rknrl.castles.game.state.players.{PlayerId, PlayerStates}
import ru.rknrl.castles.game.state.tornadoes.{Tornado, Tornadoes}
import ru.rknrl.castles.game.state.volcanoes.{Volcano, Volcanoes}
import ru.rknrl.castles.rmi.B2C.UpdateBuilding
import ru.rknrl.dto.CommonDTO.BuildingType
import ru.rknrl.dto.GameDTO.{BuildingIdDTO, PlayerIdDTO}

class Buildings(val map: Map[BuildingIdDTO, Building]) {
  def apply(id: BuildingIdDTO) = map(id)

  def updatePopulation(deltaTime: Long, config: GameConfig) = {
    def updateBuilding(b: Building) =
      b.setPopulation(config.populationAfterRegen(b, deltaTime))

    new Buildings(for ((id, b) ← map) yield id → updateBuilding(b))
  }

  def applyExitUnits(exitUnits: Iterable[ExitUnit], config: GameConfig) = {
    def updateBuilding(b: Building) =
      if (exitUnits.exists(_.fromBuildingId == b.id)) {
        Assertion.check(config.unitsToExit(b.floorPopulation) >= 1)
        b.setPopulation(config.buildingAfterUnitToExit(b.population))
      } else
        b

    new Buildings(for ((id, b) ← map) yield id → updateBuilding(b))
  }

  def applyEnterUnits(enterUnits: Iterable[EnterUnit], config: GameConfig, playerStates: PlayerStates) = {
    var newBuildings = map
    for (enterUnit ← enterUnits) {
      val b = newBuildings(enterUnit.unit.targetBuildingId)
      if (b.owner.isDefined && b.owner.get == enterUnit.unit.owner) {
        val newPopulation = config.populationAfterFriendlyUnitEnter(b, enterUnit.unit.count)
        newBuildings = newBuildings.updated(enterUnit.unit.targetBuildingId, b.setPopulation(newPopulation))
      } else {
        val buildingPlayer = if (b.owner.isDefined) Some(playerStates(b.owner.get)) else None
        val unitPlayer = playerStates(enterUnit.unit.owner)
        val (newPopulation, capture) = config.buildingAfterEnemyUnitEnter(b, enterUnit.unit, buildingPlayer, unitPlayer)
        val newOwner = if (capture) Some(enterUnit.unit.owner) else b.owner
        newBuildings = newBuildings.updated(enterUnit.unit.targetBuildingId, b.setPopulation(newPopulation).setOwner(newOwner))
      }
    }
    new Buildings(newBuildings)
  }

  def applyStrengtheningCasts(actions: Map[PlayerIdDTO, BuildingIdDTO], time: Long) = {
    def updateBuilding(b: Building) =
      if (actions.exists { case (id, buildingId) ⇒ buildingId == b.id })
        b.strength(time)
      else
        b
    new Buildings(for ((id, b) ← map) yield id → updateBuilding(b))
  }

  def cleanupStrengthening(time: Long, config: GameConfig, playerStates: PlayerStates) = {
    def updateBuilding(b: Building) = {
      if (b.strengthened && (time - b.strengtheningStartTime > config.strengtheningDuration(playerStates(b.owner.get))))
        b.unstrength()
      else
        b
    }
    new Buildings(for ((id, b) ← map) yield id → updateBuilding(b))
  }

  def canShoot(time: Long, config: GameConfig) =
    for ((id, b) ← map
         if b.prototype.getType == BuildingType.TOWER
         if time - b.lastShootTime > config.shootingInterval)
      yield b

  def applyShots(time: Long, bullets: Iterable[Bullet]) = {
    def updateBuilding(b: Building) =
      if (bullets.exists(_.building.id == b.id))
        b.shoot(time)
      else
        b

    new Buildings(for ((id, b) ← map) yield id → updateBuilding(b))
  }

  def applyFireballs(fireballs: Iterable[Fireball], playerStates: PlayerStates, config: GameConfig) =
    new Buildings(
      for ((id, b) ← map)
        yield {
          val nearFireballs = Fireballs.inRadius(fireballs, b.pos, config, playerStates)
          var newB = b
          val buildingPlayer = if (b.owner.isDefined) Some(playerStates(b.owner.get)) else None

          for (fireball ← nearFireballs)
            newB = newB.setPopulation(config.buildingPopulationAfterFireballHit(newB, playerStates(fireball.playerId), buildingPlayer))

          id → newB
        }
    )

  def applyTornadoes(tornadoes: Iterable[Tornado], playerStates: PlayerStates, config: GameConfig, time: Long) =
    new Buildings(
      for ((id, b) ← map)
        yield {
          val nearTornadoes = Tornadoes.inRadius(tornadoes, b.pos, config, playerStates, time)
          var newB = b
          val buildingPlayer = if (b.owner.isDefined) Some(playerStates(b.owner.get)) else None

          for (tornado ← nearTornadoes)
            newB = newB.setPopulation(config.buildingPopulationAfterTornadoHit(newB, playerStates(tornado.playerId), buildingPlayer))
          id → newB
        }
    )

  def applyVolcanoes(volcanoes: Iterable[Volcano], playerStates: PlayerStates, config: GameConfig) =
    new Buildings(
      for ((id, b) ← map)
        yield {
          val nearVolcanoes = Volcanoes.inRadius(volcanoes, b.pos, config, playerStates)
          var newB = b
          val buildingPlayer = if (b.owner.isDefined) Some(playerStates(b.owner.get)) else None

          for (volcano ← nearVolcanoes)
            newB = newB.setPopulation(config.buildingPopulationAfterVolcanoHit(newB, playerStates(volcano.playerId), buildingPlayer))
          id → newB
        }
    )

  def dto = for ((id, building) ← map) yield building.dto

  def updateDto = for ((id, building) ← map) yield building.updateDto
}

object Buildings {
  def getUpdateMessages(oldBuildings: Map[BuildingIdDTO, Building], newBuildings: Map[BuildingIdDTO, Building]) =
    for ((id, newBuilding) ← newBuildings
         if oldBuildings contains id
         if oldBuildings(id) differentWith newBuilding
    ) yield new UpdateBuilding(newBuilding.updateDto)
}
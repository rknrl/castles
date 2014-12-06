package ru.rknrl.castles.game.objects.buildings

import ru.rknrl.castles.game._
import ru.rknrl.castles.game.objects.Moving.{EnterUnit, ExitUnit}
import ru.rknrl.castles.game.objects.bullets.Bullet
import ru.rknrl.castles.game.objects.fireballs.{Fireball, Fireballs}
import ru.rknrl.castles.game.objects.players.{PlayerId, PlayerStates}
import ru.rknrl.castles.game.objects.tornadoes.{Tornado, Tornadoes}
import ru.rknrl.castles.game.objects.volcanoes.{Volcano, Volcanoes}
import ru.rknrl.castles.rmi.b2c.UpdateBuildingMsg

class Buildings(val buildings: Map[BuildingId, Building]) {
  def apply(id: BuildingId) = buildings(id)

  def updatePopulation(deltaTime: Long, config: GameConfig) = {
    def updateBuilding(b: Building) = {
      val add = config.getRegeneration(b.prototype) * deltaTime
      b.setPopulation(b.population + add)
    }

    new Buildings(for ((id, b) ← buildings) yield id → updateBuilding(b))
  }

  def applyExitUnits(exitUnits: Iterable[ExitUnit], config: GameConfig) = {
    def updateBuilding(b: Building) =
      if (exitUnits.exists(_.fromBuildingId == b.id)) {
        assert(config.unitsToExit(b.floorPopulation) >= 1)
        b.setPopulation(config.buildingAfterUnitToExit(b.population))
      } else
        b

    new Buildings(for ((id, b) ← buildings) yield id → updateBuilding(b))
  }

  def applyEnterUnits(enterUnits: Iterable[EnterUnit], config: GameConfig, playerStates: PlayerStates) = {
    var newBuildings = buildings
    for (enterUnit ← enterUnits) {
      val b = newBuildings(enterUnit.unit.targetBuildingId)
      if (b.owner.isDefined && b.owner.get == enterUnit.unit.owner) {
        val newPopulation = config.populationAfterFriendlyUnitEnter(b.population, enterUnit.unit.count)
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

  def applyStrengtheningCasts(actions: Map[PlayerId, BuildingId], time: Long) = {
    def updateBuilding(b: Building) =
      if (actions.exists { case (id, buildingId) ⇒ buildingId == b.id})
        b.strength(time)
      else
        b
    new Buildings(for ((id, b) ← buildings) yield id → updateBuilding(b))
  }

  def cleanupStrengthening(time: Long, config: GameConfig, playerStates: PlayerStates) = {
    def updateBuilding(b: Building) = {
      val playerState = if (b.owner.isDefined) Some(playerStates(b.owner.get)) else None
      if (b.strengthened && (time - b.strengtheningStartTime > config.strengtheningDuration(playerState)))
        b.unstrength()
      else
        b
    }
    new Buildings(for ((id, b) ← buildings) yield id → updateBuilding(b))
  }

  def canShoot(time: Long, config: GameConfig, playerStates: PlayerStates) =
    for ((id, b) ← buildings;
         playerState = if (b.owner.isDefined) Some(playerStates(b.owner.get)) else None
         if time - b.lastShootTime > config.shootingInterval(b, playerState))
    yield b

  def applyShots(time: Long, bullets: Iterable[Bullet]) = {
    def updateBuilding(b: Building) =
      if (bullets.exists(_.building.id == b.id))
        b.shoot(time)
      else
        b

    new Buildings(for ((id, b) ← buildings) yield id → updateBuilding(b))
  }

  def applyFireballs(fireballs: Iterable[Fireball], playerStates: PlayerStates, config: GameConfig) =
    new Buildings(
      for ((id, b) ← buildings)
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
      for ((id, b) ← buildings)
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
      for ((id, b) ← buildings)
      yield {
        val nearVolcanoes = Volcanoes.inRadius(volcanoes, b.pos, config, playerStates)
        var newB = b
        val buildingPlayer = if (b.owner.isDefined) Some(playerStates(b.owner.get)) else None

        for (volcano ← nearVolcanoes)
          newB = newB.setPopulation(config.buildingPopulationAfterVolcanoHit(newB, playerStates(volcano.playerId), buildingPlayer))
        id → newB
      }
    )

  def dto = for ((id, building) ← buildings) yield building.dto

  def updateDto = for ((id, building) ← buildings) yield building.updateDto
}

object Buildings {
  def getUpdateMessages(oldBuildings: Map[BuildingId, Building], newBuildings: Map[BuildingId, Building]) =
    for ((id, newBuilding) ← newBuildings
         if oldBuildings contains id
         if oldBuildings(id) differentWith newBuilding
    ) yield new UpdateBuildingMsg(newBuilding.updateDto)
}
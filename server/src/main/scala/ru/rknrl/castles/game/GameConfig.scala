package ru.rknrl.castles.game

import ru.rknrl.castles.account.objects.BuildingPrototype
import ru.rknrl.castles.config.Config.{BuildingLevelToFactor, BuildingsConfig}
import ru.rknrl.castles.game.objects.buildings.Building
import ru.rknrl.castles.game.objects.players.PlayerState
import ru.rknrl.castles.game.objects.units.GameUnit
import ru.rknrl.dto.CommonDTO.{BuildingLevel, BuildingType}

class Constants(val unitToExitFactor: Double,
                val itemCooldown: Long)

class FireballConfig(val damage: Double)

class VolcanoConfig(val damage: Double,
                    val duration: Long)

class TornadoConfig(val damage: Double,
                    val duration: Long,
                    val speed: Double)

class StrengtheningConfig(val factor: Double,
                          val duration: Long)

class ShootingConfig(val damage: Double,
                     val speed: Double,
                     val shootInterval: Long,
                     val shootRadius: Double)

class AssistanceConfig(val count: Int) {
  val buildingPrototype = new BuildingPrototype(BuildingType.HOUSE, BuildingLevel.LEVEL_1)
}

class BuildingConfig(val regeneration: Double,
                     val startPopulation: Int,
                     val stat: Stat) {
  def *(k: Double) = new BuildingConfig(regeneration, GameConfig.truncatePopulation(startPopulation * k), stat * k)
}

class GameConfig(val constants: Constants,

                 buildingsConfig: BuildingsConfig,
                 levelToFactor: BuildingLevelToFactor,

                 fireball: FireballConfig,
                 volcano: VolcanoConfig,
                 tornado: TornadoConfig,
                 strengthening: StrengtheningConfig,
                 shooting: ShootingConfig,
                 assistance: AssistanceConfig) {

  def buildingConfig(buildingType: BuildingType, buildingLevel: BuildingLevel) =
    buildingsConfig(buildingType) * levelToFactor(buildingLevel)

  val buildings: Map[BuildingPrototype, BuildingConfig] =
    (
      for (buildingType ← BuildingType.values();
           buildingLevel ← BuildingLevel.values())
      yield new BuildingPrototype(buildingType, buildingLevel) → buildingConfig(buildingType, buildingLevel)
      ).toMap


  private def toFactor(strengthened: Boolean) =
    if (strengthened) strengthening.factor else 1.0

  def getRegeneration(prototype: BuildingPrototype) = buildings(prototype).regeneration

  def getStartPopulation(prototype: BuildingPrototype) = buildings(prototype).startPopulation

  def getUnitSpeed(prototype: BuildingPrototype, player: PlayerState, strengthened: Boolean) =
    (buildings(prototype).stat + player.stat).speed

  def getAttack(prototype: BuildingPrototype, player: Option[PlayerState], strengthened: Boolean) = {
    val stat = if (player.isEmpty) buildings(prototype).stat else buildings(prototype).stat + player.get.stat
    stat.attack * toFactor(strengthened)
  }

  def getDefence(prototype: BuildingPrototype, player: Option[PlayerState], strengthened: Boolean) = {
    val stat = if (player.isEmpty) buildings(prototype).stat else buildings(prototype).stat + player.get.stat
    stat.defence * toFactor(strengthened)
  }

  def fireballRadius(player: PlayerState) = 10

  def volcanoDuration(player: PlayerState) = volcano.duration

  def tornadoRadius(player: PlayerState) = 10

  def tornadoDuration(player: PlayerState) = tornado.duration

  def tornadoSpeed = tornado.speed

  def volcanoRadius(player: PlayerState) = 10

  def strengtheningDuration(player: Option[PlayerState]) =
    5000 + (if (player.isDefined) churchesPopulationToStrengtheningDuration(player.get.churchesPopulation) else 0)

  def bulletSpeed = shooting.speed

  def shootingInterval(building: Building, player: Option[PlayerState]) = shooting.shootInterval

  def shootRadius(building: Building, player: Option[PlayerState]) = shooting.shootRadius

  def assistanceBuildingPrototype = assistance.buildingPrototype

  def assistanceCount(player: PlayerState) = churchesPopulationToAssistanceCount(player.churchesPopulation)

  /**
   * Награда★ за выигранный бой (1ое место)
   */
  val winReward = 2

  /**
   * Сколько юнитов выйдут из здания
   */
  def unitsToExit(buildingPopulation: Int): Int =
    Math.floor(buildingPopulation * constants.unitToExitFactor).toInt

  /**
   * Сколько юнитов останется в зданиии, после выхода из него отряда
   */
  def buildingAfterUnitToExit(buildingPopulation: Double): Double =
    buildingPopulation - unitsToExit(GameConfig.truncatePopulation(buildingPopulation))

  /**
   * Сколько юнитов будет в здании после входа в него дружественного отряда
   */
  def populationAfterFriendlyUnitEnter(buildingPopulation: Double, unitCount: Double) =
    buildingPopulation + unitCount

  /**
   * Сколько юнитов будет в здании после входа в него вражеского отряда
   * Вторым параметром возвращает захвачено здание или нет
   */
  def buildingAfterEnemyUnitEnter(building: Building, unit: GameUnit, buildingPlayer: Option[PlayerState], unitPlayer: PlayerState) = {
    val unitAttack = getAttack(unit.buildingPrototype, Some(unitPlayer), unit.strengthened)
    val buildingDefence = getDefence(building.prototype, buildingPlayer, building.strengthened)

    val resultPopulation = building.population - unit.count * unitAttack / buildingDefence

    val capture = resultPopulation < 0
    if (capture)
      (-resultPopulation, true)
    else
      (resultPopulation, false)
  }

  private def churchesPopulationToAttack(population: Double) = 1 + population / 30

  private def churchesPopulationToAssistanceCount(population: Double) = 20 + population / 30

  private def churchesPopulationToStrengtheningDuration(population: Double) = population / 30

  def unitCountAfterBulletHit(unitCount: Double,
                              b: Building,
                              unitPlayer: PlayerState,
                              bulletPlayer: Option[PlayerState]) = {
    val stat = if (bulletPlayer.isEmpty) buildings(b.prototype).stat else buildings(b.prototype).stat + bulletPlayer.get.stat
    unitCount - shooting.damage * stat.attack / unitPlayer.stat.defence
  }

  def unitCountAfterFireballHit(unitCount: Double,
                                fireballPlayer: PlayerState,
                                unitPlayer: PlayerState) = {
    val attack = churchesPopulationToAttack(fireballPlayer.churchesPopulation)
    unitCount - fireball.damage * attack / unitPlayer.stat.defence
  }

  def unitCountAfterVolcanoHit(unitCount: Double,
                               volcanoPlayer: PlayerState,
                               unitPlayer: PlayerState) = {
    val attack = churchesPopulationToAttack(volcanoPlayer.churchesPopulation)
    unitCount - volcano.damage * attack / unitPlayer.stat.defence
  }

  def unitCountAfterTornadoHit(unitCount: Double,
                               tornadoPlayer: PlayerState,
                               unitPlayer: PlayerState) = {
    val attack = churchesPopulationToAttack(tornadoPlayer.churchesPopulation)
    unitCount - tornado.damage * attack / unitPlayer.stat.defence
  }

  def buildingPopulationAfterFireballHit(b: Building,
                                         fireballPlayer: PlayerState,
                                         buildingPlayer: Option[PlayerState]) = {
    val buildingStat = if (buildingPlayer.isEmpty) buildings(b.prototype).stat else buildings(b.prototype).stat + buildingPlayer.get.stat
    val attack = churchesPopulationToAttack(fireballPlayer.churchesPopulation)
    Math.max(0, b.population - fireball.damage * attack / buildingStat.defence)
  }

  def buildingPopulationAfterVolcanoHit(b: Building,
                                        volcanoPlayer: PlayerState,
                                        buildingPlayer: Option[PlayerState]) = {
    val buildingStat = if (buildingPlayer.isEmpty) buildings(b.prototype).stat else buildings(b.prototype).stat + buildingPlayer.get.stat
    val attack = churchesPopulationToAttack(volcanoPlayer.churchesPopulation)
    Math.max(0, b.population - volcano.damage * attack / buildingStat.defence)
  }

  def buildingPopulationAfterTornadoHit(b: Building,
                                        tornadoPlayer: PlayerState,
                                        buildingPlayer: Option[PlayerState]) = {
    val buildingStat = if (buildingPlayer.isEmpty) buildings(b.prototype).stat else buildings(b.prototype).stat + buildingPlayer.get.stat
    val attack = churchesPopulationToAttack(tornadoPlayer.churchesPopulation)
    Math.max(0, b.population - tornado.damage * attack / buildingStat.defence)
  }

  def unitSpeedInVolcano(speed: Double) =
    speed / 2

  def unitSpeedInTornado(speed: Double) =
    speed / 2
}

object GameConfig {
  def truncatePopulation(population: Double): Int = Math.floor(population).toInt
}
//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game

import ru.rknrl.castles.Config.{BuildingLevelToFactor, BuildingsConfig}
import ru.rknrl.castles.account.state.BuildingPrototype
import ru.rknrl.castles.game.state.Stat
import ru.rknrl.castles.game.state.buildings.Building
import ru.rknrl.castles.game.state.players.PlayerState
import ru.rknrl.castles.game.state.units.GameUnit
import ru.rknrl.dto.CommonDTO.{BuildingLevel, BuildingType}

class Constants(val unitToExitFactor: Double,
                val itemCooldown: Long)

class FireballConfig(val damageVsUnit: Double,
                     val damageVsBuilding: Double,
                     val flyDuration: Long,
                     val radius: Double)

class VolcanoConfig(val damageVsUnit: Double,
                    val damageVsBuilding: Double,
                    val duration: Long,
                    val radius: Double)

class TornadoConfig(val damageVsUnit: Double,
                    val damageVsBuilding: Double,
                    val duration: Long,
                    val speed: Double,
                    val radius: Double)

class StrengtheningConfig(val factor: Double,
                          val duration: Long) {
  def tutorConfig = new StrengtheningConfig(factor, duration * 3)
}

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
  def *(k: Double) = new BuildingConfig(regeneration, GameConfig.truncatePopulation(startPopulation * k), new Stat(attack = stat.attack * k, defence = stat.defence * k, speed = stat.speed))
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

  val maxPopulation = 99

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
    (buildings(prototype).stat * player.stat).speed

  def getAttack(prototype: BuildingPrototype, player: Option[PlayerState], strengthened: Boolean) = {
    val stat = if (player.isEmpty) buildings(prototype).stat else buildings(prototype).stat + player.get.stat
    stat.attack * toFactor(strengthened)
  }

  def getDefence(prototype: BuildingPrototype, player: Option[PlayerState], strengthened: Boolean) = {
    val stat = if (player.isEmpty) buildings(prototype).stat else buildings(prototype).stat + player.get.stat
    stat.defence * toFactor(strengthened)
  }

  def fireballSplashRadius(player: PlayerState) = fireball.radius

  def fireballFlyDuration = fireball.flyDuration

  def volcanoDuration(player: PlayerState) = volcano.duration

  def volcanoRadius(player: PlayerState) = volcano.radius

  def tornadoRadius(player: PlayerState) = tornado.radius

  def tornadoDuration(player: PlayerState) = tornado.duration

  def tornadoSpeed = tornado.speed

  def strengtheningDuration(player: Option[PlayerState]) =
    strengthening.duration + (if (player.isDefined) churchesPopulationToStrengtheningDuration(player.get.churchesPopulation) else 0)

  def bulletSpeed = shooting.speed

  def shootingInterval = shooting.shootInterval

  def shootRadius = shooting.shootRadius

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
   * todo: Юниты после фаербола например могут облатать отрицательным count, поэтому проверка на Math.max
   */
  def populationAfterFriendlyUnitEnter(buildingPopulation: Double, unitCount: Double) =
    Math.max(0, Math.min(buildingPopulation + unitCount, maxPopulation))

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
    unitCount - fireball.damageVsUnit * attack / unitPlayer.stat.defence
  }

  def unitCountAfterVolcanoHit(unitCount: Double,
                               volcanoPlayer: PlayerState,
                               unitPlayer: PlayerState) = {
    val attack = churchesPopulationToAttack(volcanoPlayer.churchesPopulation)
    unitCount - volcano.damageVsUnit * attack / unitPlayer.stat.defence
  }

  def unitCountAfterTornadoHit(unitCount: Double,
                               tornadoPlayer: PlayerState,
                               unitPlayer: PlayerState) = {
    val attack = churchesPopulationToAttack(tornadoPlayer.churchesPopulation)
    unitCount - tornado.damageVsUnit * attack / unitPlayer.stat.defence
  }

  def buildingPopulationAfterFireballHit(b: Building,
                                         fireballPlayer: PlayerState,
                                         buildingPlayer: Option[PlayerState]) = {
    val buildingStat = if (buildingPlayer.isEmpty) buildings(b.prototype).stat else buildings(b.prototype).stat + buildingPlayer.get.stat
    val attack = churchesPopulationToAttack(fireballPlayer.churchesPopulation)
    Math.max(0, b.population - fireball.damageVsBuilding * attack / buildingStat.defence)
  }

  def buildingPopulationAfterVolcanoHit(b: Building,
                                        volcanoPlayer: PlayerState,
                                        buildingPlayer: Option[PlayerState]) = {
    val buildingStat = if (buildingPlayer.isEmpty) buildings(b.prototype).stat else buildings(b.prototype).stat + buildingPlayer.get.stat
    val attack = churchesPopulationToAttack(volcanoPlayer.churchesPopulation)
    Math.max(0, b.population - volcano.damageVsBuilding * attack / buildingStat.defence)
  }

  def buildingPopulationAfterTornadoHit(b: Building,
                                        tornadoPlayer: PlayerState,
                                        buildingPlayer: Option[PlayerState]) = {
    val buildingStat = if (buildingPlayer.isEmpty) buildings(b.prototype).stat else buildings(b.prototype).stat + buildingPlayer.get.stat
    val attack = churchesPopulationToAttack(tornadoPlayer.churchesPopulation)
    Math.max(0, b.population - tornado.damageVsBuilding * attack / buildingStat.defence)
  }

  def unitSpeedInVolcano(speed: Double) =
    speed / 2

  def unitSpeedInTornado(speed: Double) =
    speed / 2

  def tutorConfig = new GameConfig(
    constants,
    buildingsConfig,
    levelToFactor,
    fireball,
    volcano,
    tornado,
    strengthening.tutorConfig,
    shooting,
    assistance
  )
}

object GameConfig {
  def truncatePopulation(population: Double): Int = Math.floor(population).toInt
}
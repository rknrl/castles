//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game

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
                     val radius: Double,
                     val maxPowerBonus: Double)

class VolcanoConfig(val damageVsUnit: Double,
                    val damageVsBuilding: Double,
                    val duration: Long,
                    val radius: Double,
                    val maxPowerBonus: Double)

class TornadoConfig(val damageVsUnit: Double,
                    val damageVsBuilding: Double,
                    val duration: Long,
                    val speed: Double,
                    val radius: Double,
                    val maxPowerBonus: Double)

class StrengtheningConfig(val factor: Double,
                          val maxBonusFactor: Double,
                          val duration: Long,
                          val maxBonusDuration: Long) {
  def stat(bonus: Double) = {
    val f = factor + bonus * maxBonusFactor
    new Stat(attack = f, defence = f, speed = 1)
  }

  def tutorConfig = new StrengtheningConfig(factor, maxBonusFactor, duration * 3, maxBonusDuration)
}

class ShootingConfig(val speed: Double,
                     val shootInterval: Long,
                     val shootRadius: Double)

class AssistanceConfig(val power: Double,
                       val maxBonusPower: Double) {
  val buildingPrototype = BuildingPrototype(BuildingType.HOUSE, BuildingLevel.LEVEL_1)
}

class UnitsConfig(val house: Stat,
                  val tower: Stat,
                  val church: Stat) {

  def apply(prototype: BuildingPrototype): Stat = apply(prototype.buildingType)

  def apply(buildingType: BuildingType): Stat =
    buildingType match {
      case BuildingType.HOUSE ⇒ house
      case BuildingType.TOWER ⇒ tower
      case BuildingType.CHURCH ⇒ church
    }
}

class BuildingConfig(val regeneration: Double,
                     val startPopulation: Int,
                     val maxPopulation: Int,
                     val fortification: Double,
                     val shotPower: Option[Double])

class BuildingsConfig(val house1: BuildingConfig,
                      val house2: BuildingConfig,
                      val house3: BuildingConfig,
                      val tower1: BuildingConfig,
                      val tower2: BuildingConfig,
                      val tower3: BuildingConfig,
                      val church1: BuildingConfig,
                      val church2: BuildingConfig,
                      val church3: BuildingConfig) {

  def apply(prototype: BuildingPrototype): BuildingConfig = apply(prototype.buildingType, prototype.level)

  def apply(buildingType: BuildingType, buildingLevel: BuildingLevel): BuildingConfig =
    buildingType match {
      case BuildingType.HOUSE ⇒
        buildingLevel match {
          case BuildingLevel.LEVEL_1 ⇒ house1
          case BuildingLevel.LEVEL_2 ⇒ house2
          case BuildingLevel.LEVEL_3 ⇒ house3
        }
      case BuildingType.TOWER ⇒
        buildingLevel match {
          case BuildingLevel.LEVEL_1 ⇒ tower1
          case BuildingLevel.LEVEL_2 ⇒ tower2
          case BuildingLevel.LEVEL_3 ⇒ tower3
        }
      case BuildingType.CHURCH ⇒
        buildingLevel match {
          case BuildingLevel.LEVEL_1 ⇒ church1
          case BuildingLevel.LEVEL_2 ⇒ church2
          case BuildingLevel.LEVEL_3 ⇒ church3
        }
    }
}

class GameConfig(val constants: Constants,
                 buildings: BuildingsConfig,
                 units: UnitsConfig,
                 fireball: FireballConfig,
                 volcano: VolcanoConfig,
                 tornado: TornadoConfig,
                 strengthening: StrengtheningConfig,
                 shooting: ShootingConfig,
                 assistance: AssistanceConfig) {

  def getStartPopulation(prototype: BuildingPrototype) =
    buildings(prototype).startPopulation

  private def maxPopulation(b: Building) =
    buildings(b.prototype).maxPopulation

  def fireballSplashRadius(player: PlayerState) = fireball.radius

  def fireballFlyDuration = fireball.flyDuration

  def volcanoDuration(player: PlayerState) = volcano.duration

  def volcanoRadius(player: PlayerState) = volcano.radius

  def tornadoRadius(player: PlayerState) = tornado.radius

  def tornadoDuration(player: PlayerState) = tornado.duration

  def tornadoSpeed = tornado.speed

  def strengtheningDuration(player: PlayerState) =
    churchesToStrengtheningDuration(player)

  def bulletSpeed = shooting.speed

  def shootingInterval = shooting.shootInterval

  def shootRadius = shooting.shootRadius

  def assistanceBuildingPrototype = assistance.buildingPrototype

  def assistanceCount(b: Building, player: PlayerState) =
    churchesToAssistanceCount(b, player)

  /**
   * Награда★ за выигранный бой (1ое место)
   */
  val winReward = 2

  /**
   * Сколько юнитов будет в здании после регенерации
   */
  def populationAfterRegen(b: Building, deltaTime: Long): Double = {
    val add = buildings(b.prototype).regeneration * deltaTime
    Math.min(maxPopulation(b), b.population + add)
  }

  /**
   * Сколько юнитов выйдут из здания
   */
  def unitsToExit(buildingPopulation: Double): Int =
    Math.floor(buildingPopulation * constants.unitToExitFactor).toInt

  /**
   * Сколько юнитов останется в зданиии, после выхода из него отряда
   */
  def buildingAfterUnitToExit(buildingPopulation: Double): Double =
    buildingPopulation - unitsToExit(buildingPopulation)

  /**
   * Сколько юнитов будет в здании после входа в него дружественного отряда
   */
  def populationAfterFriendlyUnitEnter(b: Building, unitCount: Double) =
    Math.min(maxPopulation(b), b.population + unitCount)

  /**
   * Сколько юнитов будет в здании после входа в него вражеского отряда
   * Вторым параметром возвращает захвачено здание или нет
   */
  def buildingAfterEnemyUnitEnter(b: Building, unit: GameUnit, buildingPlayer: Option[PlayerState], unitPlayer: PlayerState) = {
    val attackPower = unitAttack(unit.buildingPrototype, Some(unitPlayer), unit.strengthened)
    val defencePower = unitDefence(b.prototype, buildingPlayer, b.strengthened) * fortification(b.prototype, buildingPlayer, b.strengthened)

    val leftInBuilding = b.population * defencePower - unit.count * attackPower

    val capture = leftInBuilding < 0

    val newPopulation = if (capture)
      -leftInBuilding / attackPower
    else
      leftInBuilding / defencePower

    val resultPopulation = Math.min(maxPopulation(b), newPopulation)

    (resultPopulation, capture)
  }

  def unitCountAfterBulletHit(u: GameUnit,
                              b: Building,
                              unitPlayer: PlayerState,
                              bulletPlayer: Option[PlayerState]) = {
    val damage = shotPower(b.prototype, bulletPlayer) / unitDefence(u.buildingPrototype, Some(unitPlayer), u.strengthened)
    Math.max(0, u.count - damage)
  }

  def unitCountAfterFireballHit(u: GameUnit,
                                fireballPlayer: PlayerState,
                                unitPlayer: PlayerState) = {
    val damage = fireballPowerVsUnit(fireballPlayer) / unitDefence(u.buildingPrototype, Some(unitPlayer), u.strengthened)
    Math.max(0, u.count - damage)
  }

  def unitCountAfterVolcanoHit(u: GameUnit,
                               volcanoPlayer: PlayerState,
                               unitPlayer: PlayerState) = {
    val damage = volcanoPowerVsUnit(volcanoPlayer) / unitDefence(u.buildingPrototype, Some(unitPlayer), u.strengthened)
    Math.max(0, u.count - damage)
  }

  def unitCountAfterTornadoHit(u: GameUnit,
                               tornadoPlayer: PlayerState,
                               unitPlayer: PlayerState) = {
    val damage = tornadoPowerVsUnit(tornadoPlayer) / unitDefence(u.buildingPrototype, Some(unitPlayer), u.strengthened)
    Math.max(0, u.count - damage)
  }

  def buildingPopulationAfterFireballHit(b: Building,
                                         fireballPlayer: PlayerState,
                                         buildingPlayer: Option[PlayerState]) = {
    val damage = fireballPowerVsBuilding(fireballPlayer) / unitDefence(b.prototype, buildingPlayer, b.strengthened)
    Math.max(0, b.population - damage)
  }

  def buildingPopulationAfterVolcanoHit(b: Building,
                                        volcanoPlayer: PlayerState,
                                        buildingPlayer: Option[PlayerState]) = {
    val damage = volcanoPowerVsBuilding(volcanoPlayer) / unitDefence(b.prototype, buildingPlayer, b.strengthened)
    Math.max(0, b.population - damage)
  }

  def buildingPopulationAfterTornadoHit(b: Building,
                                        tornadoPlayer: PlayerState,
                                        buildingPlayer: Option[PlayerState]) = {
    val damage = tornadoPowerVsBuilding(tornadoPlayer) / unitDefence(b.prototype, buildingPlayer, b.strengthened)
    Math.max(0, b.population - damage)
  }

  // stats

  private def shotPower(prototype: BuildingPrototype, playerState: Option[PlayerState]) =
    buildings(prototype).shotPower.get

  private def fireballPowerVsBuilding(playerState: PlayerState) = {
    val bonus = fireball.maxPowerBonus * playerState.churchesProportion
    fireball.damageVsBuilding + bonus
  }

  private def volcanoPowerVsBuilding(playerState: PlayerState) = {
    val bonus = volcano.maxPowerBonus * playerState.churchesProportion
    volcano.damageVsBuilding + bonus
  }

  private def tornadoPowerVsBuilding(playerState: PlayerState) = {
    val bonus = tornado.maxPowerBonus * playerState.churchesProportion
    tornado.damageVsBuilding + bonus
  }

  private def fireballPowerVsUnit(playerState: PlayerState) = {
    val bonus = fireball.maxPowerBonus * playerState.churchesProportion
    fireball.damageVsUnit + bonus
  }

  private def volcanoPowerVsUnit(playerState: PlayerState) = {
    val bonus = volcano.maxPowerBonus * playerState.churchesProportion
    volcano.damageVsUnit + bonus
  }

  private def tornadoPowerVsUnit(playerState: PlayerState) = {
    val bonus = tornado.maxPowerBonus * playerState.churchesProportion
    tornado.damageVsUnit + bonus
  }

  def unitSpeed(prototype: BuildingPrototype, player: PlayerState, strengthened: Boolean) =
    aggregatedStats(prototype, Some(player), strengthened).speed

  private def unitAttack(prototype: BuildingPrototype, player: Option[PlayerState], strengthened: Boolean) =
    aggregatedStats(prototype, player, strengthened).attack

  private def unitDefence(prototype: BuildingPrototype, player: Option[PlayerState], strengthened: Boolean) =
    aggregatedStats(prototype, player, strengthened).defence

  private def fortification(prototype: BuildingPrototype, player: Option[PlayerState], strengthened: Boolean) =
    buildings(prototype).fortification

  private def aggregatedStats(prototype: BuildingPrototype, player: Option[PlayerState], strengthened: Boolean) =
    units(prototype) * playerStateToStat(player) * strengtheningToStat(strengthened, player)

  private def playerStateToStat(player: Option[PlayerState]) =
    if (player.isDefined) player.get.stat else Stat.unit

  private def strengtheningToStat(strengthened: Boolean, playerState: Option[PlayerState]) =
    if (strengthened)
      strengthening.stat(playerState.get.churchesProportion)
    else
      Stat.unit

  private def churchesToAssistanceCount(b: Building, playerState: PlayerState) = {
    val bonus = assistance.maxBonusPower * playerState.churchesProportion
    buildings(b.prototype).maxPopulation * (assistance.power + bonus)
  }

  private def churchesToStrengtheningDuration(playerState: PlayerState) = {
    val bonus = strengthening.maxBonusDuration * playerState.churchesProportion
    strengthening.duration + bonus
  }

  def tutorConfig = new GameConfig(
    constants,
    buildings,
    units,
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
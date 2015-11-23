//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game

import protos.BuildingLevel.{LEVEL_1, LEVEL_2, LEVEL_3}
import protos.BuildingType.{CHURCH, HOUSE, TOWER}
import protos.{BuildingLevel, BuildingPrototype, BuildingType}
import ru.rknrl.Assertion
import ru.rknrl.castles.game.state.{Building, GameUnit}
import ru.rknrl.core.Stat

case class DamagerConfig(powerVsUnit: Double,
                         powerVsBuilding: Double,
                         maxPowerBonus: Double,
                         radius: Double) {

  Assertion.check(powerVsUnit >= 0, powerVsUnit)
  Assertion.check(powerVsBuilding >= 0, powerVsBuilding)
  Assertion.check(maxPowerBonus >= 0, maxPowerBonus)
  Assertion.check(radius > 0, radius)

  def bonused(churchesProportion: Double) =
    new DamagerConfig(
      powerVsUnit + maxPowerBonus * churchesProportion,
      powerVsBuilding + maxPowerBonus * churchesProportion,
      maxPowerBonus,
      radius
    )
}

case class Constants(itemCooldown: Long)

case class FireballConfig(damage: DamagerConfig,
                          flyDuration: Long)

case class VolcanoConfig(damage: DamagerConfig,
                         duration: Long)

case class TornadoConfig(damage: DamagerConfig,
                         duration: Long,
                         speed: Double)

case class StrengtheningConfig(factor: Double,
                               maxBonusFactor: Double,
                               duration: Long,
                               maxBonusDuration: Long) {
  def stat(churchesProportion: Double) = {
    val f = factor + churchesProportion * maxBonusFactor
    new Stat(attack = f, defence = f, speed = 1)
  }
}

case class ShootingConfig(speed: Double,
                          shootInterval: Long,
                          shootRadius: Double)

case class AssistanceConfig(power: Double,
                            maxBonusPower: Double) {
  val buildingPrototype = BuildingPrototype(HOUSE, LEVEL_1)
}

case class UnitsConfig(house: Stat,
                       tower: Stat,
                       church: Stat) {

  def apply(prototype: BuildingPrototype): Stat = apply(prototype.buildingType)

  def apply(buildingType: BuildingType): Stat =
    buildingType match {
      case HOUSE ⇒ house
      case TOWER ⇒ tower
      case CHURCH ⇒ church
    }
}

case class BuildingConfig(regeneration: Double,
                          startCount: Int,
                          maxCount: Int,
                          fortification: Double,
                          shotPower: Option[Double]) {
  Assertion.check(regeneration >= 0, regeneration)
  Assertion.check(startCount >= 0, startCount)
  Assertion.check(maxCount > 0, maxCount)
  Assertion.check(fortification >= 0, fortification)
  if (shotPower.isDefined) Assertion.check(shotPower.get > 0, shotPower.get)
}

case class BuildingsConfig(house1: BuildingConfig,
                           house2: BuildingConfig,
                           house3: BuildingConfig,
                           tower1: BuildingConfig,
                           tower2: BuildingConfig,
                           tower3: BuildingConfig,
                           church1: BuildingConfig,
                           church2: BuildingConfig,
                           church3: BuildingConfig) {

  def apply(prototype: BuildingPrototype): BuildingConfig = apply(prototype.buildingType, prototype.buildingLevel)

  def apply(buildingType: BuildingType, buildingLevel: BuildingLevel): BuildingConfig =
    buildingType match {
      case HOUSE ⇒
        buildingLevel match {
          case LEVEL_1 ⇒ house1
          case LEVEL_2 ⇒ house2
          case LEVEL_3 ⇒ house3
        }
      case TOWER ⇒
        buildingLevel match {
          case LEVEL_1 ⇒ tower1
          case LEVEL_2 ⇒ tower2
          case LEVEL_3 ⇒ tower3
        }
      case CHURCH ⇒
        buildingLevel match {
          case LEVEL_1 ⇒ church1
          case LEVEL_2 ⇒ church2
          case LEVEL_3 ⇒ church3
        }
    }
}

case class GameConfig(constants: Constants,
                      buildings: BuildingsConfig,
                      units: UnitsConfig,
                      fireball: FireballConfig,
                      volcano: VolcanoConfig,
                      tornado: TornadoConfig,
                      strengthening: StrengtheningConfig,
                      shooting: ShootingConfig,
                      assistance: AssistanceConfig) {

  def startCount(prototype: BuildingPrototype) =
    buildings(prototype).startCount

  def maxCount(b: Building) =
    buildings(b.buildingPrototype).maxCount

  def strengtheningDuration(churchesProportion: Double) = {
    val bonus = (strengthening.maxBonusDuration * churchesProportion).toLong
    strengthening.duration + bonus
  }

  def strengtheningToStat(churchesProportion: Double) =
    strengthening.stat(churchesProportion)

  def assistanceCount(b: Building, churchesProportion: Double) = {
    val bonus = assistance.maxBonusPower * churchesProportion
    buildings(b.buildingPrototype).maxCount * (assistance.power + bonus)
  }

  /**
    * Сколько юнитов будет в здании после входа в него дружественного отряда
    */
  def countAfterFriendlyUnitEnter(b: Building, unitCount: Double) =
    Math.min(maxCount(b), b.count + unitCount)

  /**
    * Сколько юнитов будет в здании после входа в него вражеского отряда
    * Вторым параметром возвращает захвачено здание или нет
    */
  def countAfterEnemyUnitEnter(b: Building, unit: GameUnit, buildingStat: Stat, unitStat: Stat) = {
    val attackPower = unitStat.attack
    val defencePower = buildingStat.defence * buildings(b.buildingPrototype).fortification

    val leftInBuilding = b.count * defencePower - unit.count * attackPower

    val capture = leftInBuilding < 0

    val newCount = if (capture)
      -leftInBuilding / attackPower
    else
      leftInBuilding / defencePower

    val resultCount = Math.min(maxCount(b), newCount)

    (resultCount, capture)
  }

  def bulletPowerVsUnit(building: Building): Double =
    buildings(building.buildingPrototype).shotPower.get

  /**
    * Награда★ за выигранный бой (1ое место)
    */
  val winReward = 2
}

object GameConfig {
  def truncatePopulation(population: Double): Int = Math.floor(population).toInt
}

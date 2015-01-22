package ru.rknrl.castles.game

import ru.rknrl.castles.Config.{BuildingLevelToFactor, BuildingsConfig}
import ru.rknrl.dto.CommonDTO.{BuildingLevel, BuildingType}

object GameConfigMock {
  def constantsMock(unitToExitFactor: Double = 0.5,
                    itemCooldown: Long = 5000) =
    new Constants(
      unitToExitFactor = unitToExitFactor,
      itemCooldown = itemCooldown
    )


  def buildingConfigMock(regeneration: Double = 0.001,
                         startPopulation: Int = 10,
                         attack: Double = 1,
                         defence: Double = 2,
                         speed: Double = 3) =
    new BuildingConfig(
      regeneration = regeneration,
      startPopulation = startPopulation,
      stat = new Stat(
        attack = attack,
        defence = defence,
        speed = speed
      )
    )

  def buildingsConfigMock(tower: BuildingConfig = buildingConfigMock(),
                          house: BuildingConfig = buildingConfigMock(),
                          church: BuildingConfig = buildingConfigMock()) =
    Map(
      BuildingType.TOWER → tower,
      BuildingType.HOUSE → house,
      BuildingType.CHURCH → church
    )


  def levelToFactorMock(level1: Double = 1, level2: Double = 1.5, level3: Double = 2) =
    Map(
      BuildingLevel.LEVEL_1 → level1,
      BuildingLevel.LEVEL_2 → level2,
      BuildingLevel.LEVEL_3 → level3
    )

  def fireballMock(damageVsUnit: Double = 1,
                   damageVsBuilding: Double = 1,
                   flyDuration: Long = 1000,
                   radius: Double = 39) =
    new FireballConfig(
      damageVsUnit = damageVsUnit,
      damageVsBuilding = damageVsBuilding,
      flyDuration = flyDuration,
      radius = radius
    )

  def volcanoMock(damageVsUnit: Double = 1,
                  damageVsBuilding: Double = 1,
                  duration: Long = 5000,
                  radius: Double = 39) =
    new VolcanoConfig(
      damageVsUnit = damageVsUnit,
      damageVsBuilding = damageVsBuilding,
      duration = duration,
      radius = radius
    )

  def tornadoMock(damageVsUnit: Double = 1,
                  damageVsBuilding: Double = 1,
                  duration: Long = 5000,
                  speed: Double = 0.05,
                  radius: Double = 39) =
    new TornadoConfig(
      damageVsUnit = damageVsUnit,
      damageVsBuilding = damageVsBuilding,
      duration = duration,
      speed = speed,
      radius = radius
    )

  def strengtheningMock(factor: Double = 1.5,
                        duration: Long = 10000) =
    new StrengtheningConfig(
      factor = factor,
      duration = duration
    )

  def shootingMock(damage: Double = 1,
                   speed: Double = 0.5,
                   shootInterval: Long = 10000,
                   shootRadius: Double = 2.2) =
    new ShootingConfig(
      damage = damage,
      speed = speed,
      shootInterval = shootInterval,
      shootRadius = shootRadius
    )

  def assistanceMock(count: Int = 50) =
    new AssistanceConfig(
      count = count
    )

  def gameConfig(constants: Constants = constantsMock(),
                 buildingsConfig: Map[BuildingType, BuildingConfig] = buildingsConfigMock(),
                 levelToFactor: Map[BuildingLevel, Double] = levelToFactorMock(),
                 fireball: FireballConfig = fireballMock(),
                 volcano: VolcanoConfig = volcanoMock(),
                 tornado: TornadoConfig = tornadoMock(),
                 strengthening: StrengtheningConfig = strengtheningMock(),
                 shooting: ShootingConfig = shootingMock(),
                 assistance: AssistanceConfig = assistanceMock()) =
    new GameConfig(
      constants,
      new BuildingsConfig(buildingsConfig),
      new BuildingLevelToFactor(levelToFactor),
      fireball,
      volcano,
      tornado,
      strengthening,
      shooting,
      assistance
    )
}

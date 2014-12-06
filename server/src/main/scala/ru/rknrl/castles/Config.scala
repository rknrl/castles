package ru.rknrl.castles

import ru.rknrl.castles.account.AccountConfig
import ru.rknrl.castles.account.objects.BuildingPrototype
import ru.rknrl.castles.game._
import ru.rknrl.core.social.SocialConfigs
import ru.rknrl.dto.CommonDTO.{BuildingLevel, BuildingType}

class Config(val social: SocialConfigs) {
  val tcpIp = "178.62.255.28"
//  val tcpIp = "127.0.0.1"
  val tcpPort = 2335

  val accountConfig = new AccountConfig(
    buildingPrices = Map(
      BuildingLevel.LEVEL_1 → 4,
      BuildingLevel.LEVEL_2 → 16,
      BuildingLevel.LEVEL_3 → 64
    ),
    itemPrice = 1,
    goldByDollar = 100
  )

  val regeneration = 0.0002
  val speed = 0.00005

  val buildings = Map(
    BuildingType.TOWER → new BuildingConfig(
      regeneration = regeneration,
      startPopulation = 10,
      new Stat(
        attack = 2,
        defence = 3,
        speed = 1 * speed
      )
    ),

    BuildingType.HOUSE → new BuildingConfig(
      regeneration = 3 * regeneration,
      startPopulation = 30,
      new Stat(
        attack = 2,
        defence = 2,
        speed = 2 * speed
      )
    ),

    BuildingType.CHURCH → new BuildingConfig(
      regeneration = 2 * regeneration,
      startPopulation = 20,
      new Stat(
        attack = 1,
        defence = 1,
        speed = 4 * speed
      )
    )
  )

  val levelToFactor = Map(
    BuildingLevel.LEVEL_1 → 1.0,
    BuildingLevel.LEVEL_2 → 1.5,
    BuildingLevel.LEVEL_3 → 2.0
  )

  def buildingConfig(buildingType: BuildingType, buildingLevel: BuildingLevel) =
    buildings(buildingType) * levelToFactor(buildingLevel)

  val buildingConfigs =
    for (buildingType ← BuildingType.values();
         buildingLevel ← BuildingLevel.values())
    yield new BuildingPrototype(buildingType, buildingLevel) → buildingConfig(buildingType, buildingLevel)

  val gameConfig = new GameConfig(

    new Constants(
      unitToExitFactor = 0.5,
      itemCooldown = 5000
    ),

    buildingConfigs.toMap,

    new FireballConfig(
      damage = 10
    ),

    new VolcanoConfig(
      damage = 10,
      duration = 2000
    ),

    new TornadoConfig(
      damage = 10,
      duration = 2000,
      speed = GameConstants.tornadoSpeed
    ),

    new StrengtheningConfig(
      factor = 1.5,
      duration = 5000
    ),

    new ShootingConfig(
      damage = 2,
      speed = 0.0004,
      shootInterval = 3000,
      shootRadius = 1
    ),

    new AssistanceConfig(
      buildingPrototype = new BuildingPrototype(BuildingType.HOUSE, BuildingLevel.LEVEL_1),
      count = 50
    )
  )
}

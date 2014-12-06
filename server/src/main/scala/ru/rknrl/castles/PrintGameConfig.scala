package ru.rknrl.castles

import ru.rknrl.castles.account.objects.BuildingPrototype
import ru.rknrl.castles.game.objects.buildings.{Building, BuildingId}
import ru.rknrl.castles.game.objects.players.{PlayerId, PlayerState}
import ru.rknrl.castles.game.objects.units.{GameUnit, UnitId}
import ru.rknrl.castles.game.{GameConfig, Stat}
import ru.rknrl.core.social.SocialConfigs
import ru.rknrl.dto.CommonDTO.{BuildingLevel, BuildingType, SkillLevel}
import ru.rknrl.utils.Point

object PrintGameConfig {
  def main(args: Array[String]): Unit = {
    val config = new Config(new SocialConfigs(None, None, None)).gameConfig

    val population = 99

    for (buildingType ← BuildingType.values();
         unitBuildingType ← BuildingType.values()) {
      print(unitBuildingType.name() + " → " + buildingType.name())
      print("\n")
      printBuildings(config, population, buildingType, unitBuildingType)
      print("\n")
    }

    println("\n\n")
    printItem("Fireball", config, population, config.buildingPopulationAfterFireballHit)
    println("\n\n")
    printItem("Tornado", config, population, config.buildingPopulationAfterTornadoHit)
    println("\n\n")
    printItem("Volcano", config, population, config.buildingPopulationAfterVolcanoHit)
  }

  def printBuildings(config: GameConfig,
                     population: Int,
                     buildingType: BuildingType,
                     unitBuildingType: BuildingType) = {

    val s = new StringBuilder

    s.append("Building")
    s.append("\t")

    for (buildingLevel ← BuildingLevel.values();
         skillLevel ← SkillLevel.values();
         strengthened ← Set(false, true)) {
      s.append(unitBuildingType.name() + buildingLevel.getNumber + "_" + skillLevel.getNumber)
      if (strengthened) s.append("_str")
      s.append("\t")
    }

    s.append("\n")


    for (buildingLevel ← BuildingLevel.values();
         skillLevel ← SkillLevel.values();
         strengthened ← Set(false, true)) {

      val building = createBuilding(buildingType, buildingLevel, population, strengthened)

      val buildingPlayerState = createBuildingPlayerState(skillLevel)

      s.append(buildingType.name() + buildingLevel.getNumber + "_" + skillLevel.getNumber)
      if (strengthened) s.append("_str")
      s.append("\t")

      for (unitBuildingLevel ← BuildingLevel.values();
           unitSkillLevel ← SkillLevel.values();
           unitStrengthened ← Set(false, true)) {

        val unitPlayerState = new PlayerState(
          attackStat(unitSkillLevel),
          churchesPopulation = 0 // unused
        )

        val unit = createUnit(unitBuildingType, unitBuildingLevel, unitStrengthened)

        val (populationAfterHit, capture) = config.buildingAfterEnemyUnitEnter(building, unit, Some(buildingPlayerState), unitPlayerState)

        s.append(GameConfig.truncatePopulation(if (capture) -populationAfterHit else populationAfterHit))
        s.append("\t")
      }

      s.append("\n")
    }

    print(s.toString())
  }


  def createUnit(unitBuildingType: BuildingType, unitBuildingLevel: BuildingLevel, unitStrengthened: Boolean): GameUnit = {
    new GameUnit(
      buildingPrototype = new BuildingPrototype(unitBuildingType, unitBuildingLevel),
      count = 50,
      strengthened = unitStrengthened,

      id = new UnitId(0), // unused
      startPos = new Point(0, 0), // unused
      endPos = new Point(0, 0), // unused
      startTime = 0, // unused
      speed = 0.1, // unused
      targetBuildingId = new BuildingId(0), // unused
      owner = new PlayerId(0) // unused
    )
  }

  val churchesMaxCount = 10

  def printItem(item: String,
                config: GameConfig,
                population: Int,
                getPopulationAfterHit: (Building, PlayerState, Option[PlayerState]) ⇒ Double) = {

    val s = new StringBuilder

    printHeader(s, item)

    for (buildingType ← BuildingType.values();
         buildingLevel ← BuildingLevel.values();
         skillLevel ← SkillLevel.values();
         strengthened ← Set(false, true)) {

      val building = createBuilding(buildingType, buildingLevel, population, strengthened)

      val buildingPlayerState = createBuildingPlayerState(skillLevel)

      s.append(buildingType.name() + buildingLevel.getNumber + "_" + skillLevel.getNumber)
      if (strengthened) s.append("_str")
      s.append("\t")

      for (churchesPopulation ← 0 to churchesMaxCount) {
        val attackPlayerState = createItemPlayerState(churchesPopulation)

        val populationAfterHit = getPopulationAfterHit(building, attackPlayerState, Some(buildingPlayerState))

        s.append(GameConfig.truncatePopulation(populationAfterHit))
        s.append("\t")
      }

      s.append("\n")
    }

    print(s.toString())
  }


  def printHeader(s: StringBuilder, item: String): Unit = {
    s.append("Building")
    s.append("\t")

    for (churchesPopulation ← 0 to churchesMaxCount) {
      s.append(item + churchesPopulation)
      s.append("\t")
    }
    s.append("\n")
  }

  def createBuilding(buildingType: BuildingType, buildingLevel: BuildingLevel, population: Int, strengthened: Boolean) =
    new Building(
      prototype = new BuildingPrototype(buildingType, buildingLevel),
      population = population,
      strengthened = strengthened,

      id = new BuildingId(0), // unused
      x = 0, y = 0, // unused
      owner = None, // unused
      strengtheningStartTime = 0, // unused
      lastShootTime = 0 // unused
    )

  def createBuildingPlayerState(skillLevel: SkillLevel) =
    new PlayerState(
      defenceStat(skillLevel),
      churchesPopulation = 0 // unused
    )

  def createItemPlayerState(churchesPopulation: Int) =
    new PlayerState(
      new Stat(attack = 0, defence = 0, speed = 0), // unused
      churchesPopulation
    )

  def defenceStat(skillLevel: SkillLevel) = new Stat(attack = 0, defence = skillLevel.getNumber, speed = 0)

  def attackStat(skillLevel: SkillLevel) = new Stat(attack = skillLevel.getNumber, defence = 0, speed = 0)
}

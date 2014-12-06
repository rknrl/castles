package ru.rknrl.castles.game

import ru.rknrl.castles.account.objects.{BuildingPrototype, IJ}
import ru.rknrl.castles.game.objects.buildings.Building
import ru.rknrl.dto.CommonDTO.{BuildingLevel, BuildingType}
import ru.rknrl.utils.BuildingIdIterator

object MapGenerator {
  private def exists(newPos: IJ, positions: Iterable[IJ]): Boolean = {
    for (pos ← positions)
      if (newPos near pos) return true
    false
  }

  private def getAllPositions(positions: Iterable[IJ], h: Int, v: Int) = {
    var newPositions = List.empty[IJ]

    for (i ← 0 until h; j ← 0 until v) {
      val pos = new IJ(i, j)
      if (!exists(pos, positions) && !exists(pos, newPositions))
        newPositions = newPositions :+ pos
    }

    newPositions
  }

  private def pickRandomFromList[T](list: List[T], count: Int) = {
    var indices = (0 until count).toList
    var randomIndices = List.empty[Int]
    for (_ ← 0 until count) {
      val i = Math.floor(Math.random() * indices.size).toInt
      randomIndices = randomIndices :+ indices(i)
      indices = indices.patch(i, Nil, 1)
    }
    randomIndices.map(i ⇒ list(i))
  }

  private def getRandomBuildingPositions(positions: Iterable[IJ], h: Int, v: Int) = {
    val allPositions = getAllPositions(positions, h, v)
    val size = allPositions.size

    val randomCount = Math.floor(size * 0.5 + (size * 0.3) * Math.random()).toInt
    pickRandomFromList(allPositions, randomCount)
  }

  private def randomBuildingPrototype = {
    val types = Map(
      0 → BuildingType.HOUSE,
      1 → BuildingType.TOWER,
      2 → BuildingType.CHURCH
    )

    val levels = Map(
      0 → BuildingLevel.LEVEL_1,
      1 → BuildingLevel.LEVEL_2,
      2 → BuildingLevel.LEVEL_3
    )

    val typeIndex = Math.floor(Math.random() * BuildingType.values().size).toInt
    val levelIndex = Math.floor(Math.random() * BuildingLevel.values().size).toInt
    val buildingType = types(typeIndex)
    val buildingLevel = levels(levelIndex)
    new BuildingPrototype(buildingType, buildingLevel)
  }

  def getRandomBuildings(positions: Iterable[IJ], h: Int, v: Int, buildingIdIterator: BuildingIdIterator, config: GameConfig) = {
    val randomPositions = getRandomBuildingPositions(positions, h, v)
    for (pos ← randomPositions)
    yield {
      val prototype = randomBuildingPrototype
      new Building(
        id = buildingIdIterator.next,
        prototype = prototype,
        pos.toXY.x, pos.toXY.y,
        population = config.getStartPopulation(prototype),
        owner = None,
        strengthened = false,
        strengtheningStartTime = 0,
        lastShootTime = 0
      )
    }
  }
}

//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state.area

import java.util.Random

import ru.rknrl.castles.account.state.{BuildingPrototype, IJ}
import ru.rknrl.castles.game.GameConfig
import ru.rknrl.castles.game.state.BuildingIdIterator
import ru.rknrl.castles.game.state.buildings.Building
import ru.rknrl.dto.CommonDTO.{BuildingLevel, BuildingType}

object MapGenerator {
  private def exists(newPos: IJ, positions: Iterable[IJ]) =
    positions.exists(_ near newPos)

  private def getAllPositions(positions: Iterable[IJ], h: Int, v: Int) = {
    var newPositions = List.empty[IJ]

    for (i ← 0 until h; j ← 0 until v) {
      val pos = new IJ(h - 1 - i, v - 1 - j) // начинаем заполнять от центра
      if (!exists(pos, positions) && !exists(pos, newPositions))
        newPositions = newPositions :+ pos
    }

    newPositions
  }

  private def pickRandomFromList[T](list: List[T], count: Int) = {
    var indices = (0 until count).toList
    val rnd = new Random()
    var randomIndices = List.empty[Int]
    for (_ ← 0 until count) {
      val i = rnd.nextInt(indices.size)
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
    val types = BuildingType.values()
    val levels = BuildingLevel.values()

    val typeIndex = Math.floor(Math.random() * types.size).toInt
    val levelIndex = Math.floor(Math.random() * levels.size).toInt
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
        pos.toXY,
        population = config.getStartPopulation(prototype),
        owner = None,
        strengthened = false,
        strengtheningStartTime = 0,
        lastShootTime = 0
      )
    }
  }
}

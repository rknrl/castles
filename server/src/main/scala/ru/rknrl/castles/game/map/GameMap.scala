//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.map

import java.io.File

import ru.rknrl.castles.account.state.{BuildingPrototype, IJ}
import ru.rknrl.castles.game.GameConfig
import ru.rknrl.castles.game.map.GameMap.random
import ru.rknrl.castles.game.state.BuildingIdIterator
import ru.rknrl.castles.game.state.buildings.Building
import ru.rknrl.dto.CommonDTO.{BuildingLevel, BuildingType}

import scala.io.Source

class MapCell(val i: Int,
              val j: Int,
              val buildingType: Option[BuildingType],
              val buildingLevel: Option[BuildingLevel])

class GameMap(val cells: Iterable[MapCell]) {

  def buildings(iterator: BuildingIdIterator, config: GameConfig) =
    for (cell ← cells) yield {
      val buildingType = cell.buildingType.getOrElse(random(BuildingType.values()))
      val buildingLevel = cell.buildingLevel.getOrElse(random(BuildingLevel.values()))
      val prototype = new BuildingPrototype(buildingType, buildingLevel)

      new Building(
        iterator.next,
        prototype,
        new IJ(cell.i, cell.j).toXY,
        config.getStartPopulation(prototype),
        owner = None,
        strengthened = false,
        strengtheningStartTime = 0,
        lastShootTime = 0
      )
    }
}

class GameMaps(val big: Array[GameMap],
               val small: Array[GameMap]) {
  def random(isBig: Boolean) =
    if (isBig) GameMap.random(big) else GameMap.random(small)
}

object GameMap {
  def random[T](list: Array[T]): T =
    list((Math.random() * list.length).toInt)

  private def toBuildingType(s: String) =
    s match {
      case "random" ⇒ None
      case "house" ⇒ Some(BuildingType.HOUSE)
      case "tower" ⇒ Some(BuildingType.TOWER)
      case "church" ⇒ Some(BuildingType.CHURCH)
    }

  private def toBuildingLevel(s: String) =
    s match {
      case "random" ⇒ None
      case "level1" ⇒ Some(BuildingLevel.LEVEL_1)
      case "level2" ⇒ Some(BuildingLevel.LEVEL_2)
      case "level3" ⇒ Some(BuildingLevel.LEVEL_3)
    }

  private def lineToCell(line: String) = {
    val split = line.split('\t')
    assert(split.length == 4, split.length)

    new MapCell(
      i = split(0).toInt,
      j = split(1).toInt,
      buildingType = toBuildingType(split(2)),
      buildingLevel = toBuildingLevel(split(3))
    )
  }

  def fromString(s: String) =
    new GameMap(s.split('\n').map(lineToCell))

  def fromFile(file: File) = {
    println("map file " + file.getName)
    fromString(Source.fromFile(file).mkString)
  }
}

object GameMaps {
  private def filesToMaps(dir: String) =
    new File(dir).listFiles.map(GameMap.fromFile)

  def fromFiles(dir: String) =
    new GameMaps(filesToMaps(dir + "big"), filesToMaps(dir + "small"))
}
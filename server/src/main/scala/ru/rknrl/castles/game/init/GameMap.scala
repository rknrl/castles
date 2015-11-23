//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.init

import java.io.File

import protos.{BuildingLevel, BuildingPrototype, BuildingType}
import ru.rknrl.castles.account.IJ
import ru.rknrl.castles.game.GameConfig
import ru.rknrl.castles.game.state.{Building, BuildingIdIterator}
import ru.rknrl.{Assertion, RandomUtil}

import scala.io.Source

class MapCell(val i: Int,
              val j: Int,
              val buildingType: Option[BuildingType],
              val buildingLevel: Option[BuildingLevel])

class GameMap(val cells: Iterable[MapCell]) {

  private def topLeftBuildings(iterator: BuildingIdIterator, config: GameConfig) =
    for (cell ← cells) yield {
      val buildingType = cell.buildingType.getOrElse(RandomUtil.random(BuildingType.values))
      val buildingLevel = cell.buildingLevel.getOrElse(RandomUtil.random(BuildingLevel.values))
      val prototype = BuildingPrototype(buildingType, buildingLevel)

      val stat = config.units(prototype)

      new Building(
        id = iterator.next,
        buildingPrototype = prototype,
        pos = IJ(cell.i, cell.j).centerXY,
        count = config.startCount(prototype),
        owner = None,
        strengthening = None,
        lastShootTime = 0,
        buildingStat = stat
      )
    }

  def buildings(gameArea: GameArea, iterator: BuildingIdIterator, config: GameConfig) = {
    val topLeft = topLeftBuildings(iterator, config)
    topLeft ++ mirrorBuildingsBigMap(gameArea, topLeft, iterator)
  }

  //  private def mirrorBuildingsSmallMap(gameArea: GameArea, topBuildings: Iterable[Building], buildingIdIterator: BuildingIdIterator) =
  //    for (b ← topBuildings)
  //      yield {
  //        val pos = gameArea.mirrorH(gameArea.mirrorV(b.pos))
  //        new Building(buildingIdIterator.next, b.prototype, pos, b.population, b.owner, b.strengthened, b.strengtheningStartTime, b.lastShootTime)
  //      }

  private def mirrorBuildingsBigMap(gameArea: GameArea, topLeftBuildings: Iterable[Building], buildingIdIterator: BuildingIdIterator) = {
    // attention: только для карт нечетного размера
    val half = IJ(gameArea.v / 2, gameArea.h / 2).centerXY

    def getParts(b: Building) =
      if (b.pos.x == half.x && b.pos.y == half.y)
        List()
      else if (b.pos.x == half.x)
        List(2)
      else if (b.pos.y == half.y)
        List(1)
      else
        List(1, 2, 3)

    for (b ← topLeftBuildings;
         part ← getParts(b))
      yield {
        val pos = part match {
          case 1 ⇒ gameArea.mirrorH(b.pos)
          case 2 ⇒ gameArea.mirrorV(b.pos)
          case 3 ⇒ gameArea.mirrorH(gameArea.mirrorV(b.pos))
        }

        new Building(
          id = buildingIdIterator.next,
          buildingPrototype = b.buildingPrototype,
          pos = pos,
          count = b.count,
          owner = b.owner,
          strengthening = b.strengthening,
          lastShootTime = b.lastShootTime,
          buildingStat = b.buildingStat
        )
      }
  }
}

class GameMaps(val big: Array[GameMap],
               val small: Array[GameMap],
               val bigTutor: GameMap,
               val smallTutor: GameMap) {
  def random(isBig: Boolean) =
    if (isBig) RandomUtil.random(big) else RandomUtil.random(small)

  def tutor(isBig: Boolean) =
    if (isBig) bigTutor else smallTutor
}

object GameMap {

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
    Assertion.check(split.length == 4, split.length)

    new MapCell(
      i = split(0).toInt,
      j = split(1).toInt,
      buildingType = toBuildingType(split(2)),
      buildingLevel = toBuildingLevel(split(3))
    )
  }

  def fromString(s: String) =
    new GameMap(s.split('\n').map(lineToCell))

  def fromFile(file: String): GameMap = {
    println("map file " + file)
    fromString(Source.fromFile(file, "UTF-8").mkString)
  }

  def fromFile(file: File): GameMap = {
    println("map file " + file.getName)
    fromString(Source.fromFile(file, "UTF-8").mkString)
  }
}

object GameMaps {
  private def filesToMaps(dir: String) =
    new File(dir).listFiles.map(GameMap.fromFile)

  def fromFiles(dir: String) =
    new GameMaps(filesToMaps(dir + "big"), filesToMaps(dir + "small"), GameMap.fromFile(dir + "big/tutor"), GameMap.fromFile(dir + "small/tutor"))
}
object BuildingType extends Enumeration {
  type BuildingType = Value
  val Tower, House, Church = Value
}

object MagicItemType extends Enumeration {
  type MagicItemType = Value
  val Fireball, Volcano, Tornado, Assistance, Strengthening = Value
}

object PlayerId extends Enumeration {
  type PlayerId = Value
  val playerId1, playerId2 = Value
}

import BuildingType._
import MagicItemType.MagicItemType
import PlayerId._

class Stat(val attack: Double,
           val defence: Double,
           val speed: Double)

class Config(val magicItemMultiplier: Double) {
  val maxPopulation = 99

  def magicItemAttack(churchesPopulation: Double) = magicItemMultiplier * churchesPopulation

  def strengtheningMultiplier = 1.5

  def assistanceCount = 10
}

class Building(val id: Int,
               val i: Double,
               val j: Double,
               val buildingType: BuildingType,
               val population: Double,
               val owner: Option[PlayerId]) {

  def setPopulation(value: Double) = new Building(id, i, j, buildingType, value, owner)

  def setOwner(playerId: PlayerId) = new Building(id, i, j, buildingType, population, Some(playerId))
}

class Buildings(val list: Iterable[Building]) {
  def applyUnitsToBuildings(units: Iterable[GameUnit]) =
    new Buildings(
      for (b ← list)
      yield {
        val exitUnits = units.filter(_.fromBuilding.id == b.id)
        val enterUnits = units.filter(_.toBuilding.id == b.id)
        var newB = b

        for (exitUnit ← exitUnits)
          newB = newB.setPopulation(newB.population / 2)

        for (enterUnit ← enterUnits)
          newB = newB.setPopulation(newB.population - enterUnit.fromBuilding.population / 2)

        newB
      }
    )

  def appyStrengthening(str: Iterable[Strengthening]) =
    new Buildings(list)

  def populateBuildings() =
    new Buildings(for (b ← list) yield b.setPopulation(Math.min(99, b.population + 0.01)))

  def getRandomBuilding(buildings: List[Building]) =
    buildings((Math.random() * buildings.size).toInt)

  def playerBuildings(playerId: PlayerId) =
    list.filter(b ⇒ b.owner.isDefined && b.owner.get == playerId).toList

  def buildingsCount(playerId: PlayerId) =
    playerBuildings(playerId).size
}


class GameUnit(val fromBuilding: Building,
               val toBuilding: Building,
               val startStep: Int)

class Units(val list: Iterable[GameUnit]) {
  def add(newUnits: Iterable[GameUnit]) = new Units(list ++ newUnits)

  def cleanup() = new Units(list)
}

class MagicItemVsBuilding(itemType: MagicItemType, vsBuilding: Building)

class MagicItemVsUnits(itemType: MagicItemType, vsUnits: Building)

class Strengthening(toBuilding: Building)

class Assistance(toBuilding: Building)


class GameState(val step: Int,
                val buildings: Buildings,
                val units: Units) {

  def update() = {
    val magicItemVsBuilding = produceMagicItemVsBuilding
    val magicItemVsUnits = produceMagicItemVsUnits
    val strengthening = produceStrengthening
    val assistance = produceAssistance

    val newBuildings = buildings
      .populateBuildings()
      .applyUnitsToBuildings(produceUnits)
      .appyStrengthening(strengthening)

    new GameState(step + 1, newBuildings, units)
  }

  def produceUnits = {
    val fromBuilding = buildings.getRandomBuilding(buildings.playerBuildings(playerId1))
    val toBuilding = buildings.getRandomBuilding(buildings.list.toList)
    List(new GameUnit(fromBuilding, toBuilding, step))
  }

  def produceMagicItemVsBuilding = {
    List.empty[MagicItemVsBuilding]
  }

  def produceMagicItemVsUnits = {
    List.empty[MagicItemVsUnits]
  }

  def produceStrengthening = {
    List.empty[Strengthening]
  }

  def produceAssistance = {
    List.empty[Assistance]
  }

  def winner =
    if (buildings.buildingsCount(playerId2) == 0)
      Some(playerId1)
    else if (buildings.buildingsCount(playerId1) == 0)
      Some(playerId2)
    else
      None
}


class BalanceMiner {
  def main(args: Array[String]) {
    val initBuildings = new Buildings(List.empty)
    val initUnits = new Units(List.empty)
    var gameState = new GameState(0, initBuildings, initUnits)

    while (gameState.winner.isEmpty)
      gameState = gameState.update()
  }
}

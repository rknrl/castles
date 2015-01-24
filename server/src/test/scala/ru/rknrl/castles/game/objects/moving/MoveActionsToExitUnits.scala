package ru.rknrl.castles.game.objects.moving

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.account.objects.BuildingPrototype
import ru.rknrl.castles.game.objects.{PlayerStateTest, Moving}
import ru.rknrl.castles.game.objects.buildings.{BuildingId, BuildingTest, Buildings}
import ru.rknrl.castles.game.objects.players.PlayerId
import ru.rknrl.castles.mock.GameConfigMock
import ru.rknrl.dto.CommonDTO.{BuildingLevel, BuildingType}
import ru.rknrl.dto.GameDTO.MoveDTO

import scala.collection.JavaConverters._

class MoveActionsToExitUnits extends FlatSpec with Matchers {
  "moveActions→exitUnits" should "work with empty list" in {
    val buildings = new Buildings(Map.empty)
    val config = GameConfigMock.gameConfig()
    Moving.`moveActions→exitUnits`(Map.empty, buildings, config).size should be(0)
  }

  def moveDto(fromBuildingsId: Iterable[BuildingId], toBuildingId: BuildingId) =
    MoveDTO.newBuilder()
      .addAllFromBuildings(fromBuildingsId.map(_.dto).asJava)
      .setToBuilding(toBuildingId.dto)
      .build()

  "moveActions→exitUnits" should "return units" in {
    val player0 = new PlayerId(0)
    val player1 = new PlayerId(1)

    val id0 = new BuildingId(0)
    val id1 = new BuildingId(1)

    val b0 = BuildingTest.building(
      id = id0,
      owner = Some(player0),
      population = 90,
      prototype = new BuildingPrototype(BuildingType.HOUSE, BuildingLevel.LEVEL_1)
    )

    val b1 = BuildingTest.building(
      id = id1,
      owner = Some(player1),
      population = 40.14,
      prototype = new BuildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_2),
      strengthened = true
    )

    val buildings = new Buildings(Map(id0 → b0, id1 → b1))

    val move1 = player0 → moveDto(List(id0), id1)
    val move2 = player1 → moveDto(List(id1), id0)

    val config = GameConfigMock.gameConfig()

    val exits = Moving.`moveActions→exitUnits`(Map(move1, move2), buildings, config).toList

    exits.size should be(2)

    exits(0).playerId should be(player0)
    exits(0).fromBuildingId should be(id0)
    exits(0).toBuildingId should be(id1)

    exits(1).playerId should be(player1)
    exits(1).fromBuildingId should be(id1)
    exits(1).toBuildingId should be(id0)
  }

  "moveActions→exitUnits" should "throw AssertionError if source building == target building" in {
    val player0 = new PlayerId(0)

    val id0 = new BuildingId(0)

    val b0 = BuildingTest.building(
      id = id0,
      owner = Some(player0),
      population = 90,
      prototype = new BuildingPrototype(BuildingType.HOUSE, BuildingLevel.LEVEL_1)
    )

    val buildings = new Buildings(Map(id0 → b0))

    val move1 = player0 → moveDto(List(id0), id0)

    val config = GameConfigMock.gameConfig()

    a[AssertionError] should be thrownBy {
      val exits = Moving.`moveActions→exitUnits`(Map(move1), buildings, config).toList
    }
  }

  "moveActions→exitUnits" should "throw AssertionError if sourceBuilding.owner != move.playerId" in {
    val player0 = new PlayerId(0)
    val player1 = new PlayerId(1)

    val id0 = new BuildingId(0)
    val id1 = new BuildingId(1)

    val b0 = BuildingTest.building(
      id = id0,
      owner = Some(player0),
      population = 90,
      prototype = new BuildingPrototype(BuildingType.HOUSE, BuildingLevel.LEVEL_1)
    )

    val b1 = BuildingTest.building(
      id = id1,
      owner = Some(player1),
      population = 40.14,
      prototype = new BuildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_2),
      strengthened = true
    )

    val buildings = new Buildings(Map(id0 → b0, id1 → b1))

    val move1 = player1 → moveDto(List(id0), id1)

    val config = GameConfigMock.gameConfig()

    a[AssertionError] should be thrownBy {
      val exits = Moving.`moveActions→exitUnits`(Map(move1), buildings, config).toList
    }
  }

  "moveActions→exitUnits" should "not return exits if unitToGo count < 1" in {
    val player0 = new PlayerId(0)
    val player1 = new PlayerId(1)

    val id0 = new BuildingId(0)
    val id1 = new BuildingId(1)

    val b0 = BuildingTest.building(
      id = id0,
      owner = Some(player0),
      population = 2,
      prototype = new BuildingPrototype(BuildingType.HOUSE, BuildingLevel.LEVEL_1)
    )

    val b1 = BuildingTest.building(
      id = id1,
      owner = Some(player1),
      population = 40.14,
      prototype = new BuildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_2),
      strengthened = true
    )

    val buildings = new Buildings(Map(id0 → b0, id1 → b1))

    val move0 = player0 → moveDto(List(id0), id1)

    val config = GameConfigMock.gameConfig(constants = GameConfigMock.constantsMock(unitToExitFactor = 0.4))

    Moving.`moveActions→exitUnits`(Map(move0), buildings, config).size should be(0)

    val config2 = GameConfigMock.gameConfig(constants = GameConfigMock.constantsMock(unitToExitFactor = 0.5))

    Moving.`moveActions→exitUnits`(Map(move0), buildings, config2).size should be(1)
  }

  "moveActions→exitUnits" should "work with many source buildings" in {
    val player0 = new PlayerId(0)
    val player1 = new PlayerId(1)

    val id0 = new BuildingId(0)
    val id1 = new BuildingId(1)
    val id2 = new BuildingId(2)

    val b0 = BuildingTest.building(
      id = id0,
      owner = Some(player0),
      population = 90,
      prototype = new BuildingPrototype(BuildingType.HOUSE, BuildingLevel.LEVEL_1)
    )

    val b1 = BuildingTest.building(
      id = id1,
      owner = Some(player0),
      population = 40.14,
      prototype = new BuildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_2),
      strengthened = true
    )

    val b2 = BuildingTest.building(
      id = id2,
      owner = Some(player1),
      population = 40.14,
      prototype = new BuildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_2),
      strengthened = true
    )

    val buildings = new Buildings(Map(id0 → b0, id1 → b1, id2 → b1))

    val move0 = player0 → moveDto(List(id0, id1), id2)

    val config = GameConfigMock.gameConfig()

    val exits = Moving.`moveActions→exitUnits`(Map(move0), buildings, config).toList

    exits.size should be(2)

    exits(0).playerId should be(player0)
    exits(0).fromBuildingId should be(id0)
    exits(0).toBuildingId should be(id2)

    exits(1).playerId should be(player0)
    exits(1).fromBuildingId should be(id1)
    exits(1).toBuildingId should be(id2)
  }

}

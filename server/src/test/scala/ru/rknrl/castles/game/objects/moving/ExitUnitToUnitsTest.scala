import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.account.objects.BuildingPrototype
import ru.rknrl.castles.game._
import ru.rknrl.castles.game.objects.Moving.ExitUnit
import ru.rknrl.castles.game.objects.buildings.{BuildingId, BuildingTest, Buildings}
import ru.rknrl.castles.game.objects.players.PlayerId
import ru.rknrl.castles.game.objects.{Moving, PlayerStateTest}
import ru.rknrl.dto.CommonDTO.{BuildingLevel, BuildingType}

class ExitUnitToUnitsTest extends FlatSpec with Matchers {
  val states = PlayerStateTest.playerStates

  "exitUnits->units" should "work with empty list" in {
    val buildings = new Buildings(Map.empty)
    val config = GameConfigMock.gameConfig(constants = GameConfigMock.constantsMock())
    val unitIdIterator = new UnitIdIterator
    Moving.`exitUnit→units`(List.empty, buildings, config, unitIdIterator, states, 1990).size should be(0)
  }

  "exitUnits->units" should "return units" in {
    val player0 = new PlayerId(0)
    val player1 = new PlayerId(1)

    val id0 = new BuildingId(0)
    val id1 = new BuildingId(1)

    val prototype0 = new BuildingPrototype(BuildingType.HOUSE, BuildingLevel.LEVEL_1)
    val strengthened0 = false

    val b0 = BuildingTest.building(
      id = id0,
      owner = Some(player0),
      population = 90,
      prototype = prototype0,
      strengthened = strengthened0
    )

    val prototype1 = new BuildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_2)
    val strengthened1 = false

    val b1 = BuildingTest.building(
      id = id1,
      owner = Some(player1),
      population = 40.14,
      prototype = prototype1,
      strengthened = strengthened1
    )
    val buildings = new Buildings(Map(id0 → b0, id1 → b1))

    val exit1 = new ExitUnit(player0, id0, id1)
    val exit2 = new ExitUnit(player1, id1, id0)

    val config = GameConfigMock.gameConfig(
      constants = GameConfigMock.constantsMock(unitToExitFactor = 0.4),
      buildingsConfig = GameConfigMock.buildingsConfigMock(
        house = GameConfigMock.buildingConfigMock(speed = 0.007),
        tower = GameConfigMock.buildingConfigMock(speed = 0.008)
      )
    )

    val unitIdIterator = new UnitIdIterator
    val units = Moving.`exitUnit→units`(List(exit1, exit2), buildings, config, unitIdIterator, states, 1990).toList

    units.size should be(2)

    units(0).id.id should be(1)
    units(0).buildingPrototype should be(b0.prototype)
    units(0).count should be(config.unitsToExit(b0.floorPopulation))
    units(0).startPos should be(b0.pos)
    units(0).endPos should be(b1.pos)
    units(0).startTime should be(1990)
    units(0).speed should be(config.getUnitSpeed(prototype0, states.states(player0), strengthened0))
    units(0).targetBuildingId should be(id1)
    units(0).owner should be(player0)
    units(0).strengthened should be(false)

    units(1).id.id should be(2)
    units(1).buildingPrototype should be(b1.prototype)
    units(1).count should be(config.unitsToExit(b1.floorPopulation))
    units(1).startPos should be(b1.pos)
    units(1).endPos should be(b0.pos)
    units(1).startTime should be(1990)
    units(1).speed should be(config.getUnitSpeed(prototype1, states.states(player1), strengthened1))
    units(1).targetBuildingId should be(id0)
    units(1).owner should be(player1)
    units(1).strengthened should be(true)
  }
}

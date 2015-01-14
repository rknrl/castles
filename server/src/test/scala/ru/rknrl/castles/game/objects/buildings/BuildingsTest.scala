package ru.rknrl.castles.game.objects.buildings

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.account.objects.BuildingPrototype
import ru.rknrl.castles.game._
import ru.rknrl.castles.game.objects.Moving.{EnterUnit, ExitUnit}
import ru.rknrl.castles.game.objects.PlayerStateTest
import ru.rknrl.castles.game.objects.bullets.Bullet
import ru.rknrl.castles.game.objects.players.PlayerId
import ru.rknrl.castles.game.objects.units.{GameUnitTest, GameUnits}
import ru.rknrl.dto.CommonDTO.{BuildingLevel, BuildingType}

class BuildingsTest extends FlatSpec with Matchers {
  private val id0 = new BuildingId(0)
  private val id1 = new BuildingId(1)
  private val id2 = new BuildingId(2)
  private val b0 = BuildingTest.building(id0)
  private val b1 = BuildingTest.building(id1)
  private val b2 = BuildingTest.building(id2)

  "updatePopulation" should "change buildings" in {
    val b0 = BuildingTest.building(
      id0,
      prototype = new BuildingPrototype(BuildingType.HOUSE, BuildingLevel.LEVEL_1),
      population = 100.1
    )

    val b1 = BuildingTest.building(
      id1,
      prototype = new BuildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_1),
      population = 200.2
    )

    val b2 = BuildingTest.building(
      id2,
      prototype = new BuildingPrototype(BuildingType.CHURCH, BuildingLevel.LEVEL_1),
      population = 300.3
    )

    val buildingsConfig = GameConfigMock.buildingsConfigMock(
      house = GameConfigMock.buildingConfigMock(regeneration = 0.0001),
      tower = GameConfigMock.buildingConfigMock(regeneration = 0.0002),
      church = GameConfigMock.buildingConfigMock(regeneration = 0.0003)
    )

    val config = GameConfigMock.gameConfig(
      strengthening = new StrengtheningConfig(factor = 1.5, duration = 10000),
      buildingsConfig = buildingsConfig
    )

    val updated = new Buildings(Map(id0 → b0, id1 → b1, id2 → b2)).updatePopulation(2, config)

    updated(id0).population should be(100.1 + 0.0001 * 2)
    updated(id1).population should be(200.2 + 0.0002 * 2)
    updated(id2).population should be(300.3 + 0.0003 * 2)
  }

  "applyExitUnits" should "work with empty list" in {
    val b0 = BuildingTest.building(
      id0,
      population = 100.1
    )

    val b1 = BuildingTest.building(
      id1,
      population = 200.2
    )

    val b2 = BuildingTest.building(
      id2,
      population = 300.3
    )

    val config = GameConfigMock.gameConfig(
      constants = GameConfigMock.constantsMock(unitToExitFactor = 0.5)
    )

    val updated = new Buildings(Map(id0 → b0, id1 → b1, id2 → b2)).applyExitUnits(List.empty, config)

    updated(id0).population should be(100.1)
    updated(id1).population should be(200.2)
    updated(id2).population should be(300.3)
  }

  "applyExitUnits" should "throw AssertionError if unitToGo < 1" in {
    val b0 = BuildingTest.building(
      id0,
      population = 2
    )

    val config = GameConfigMock.gameConfig(constants = GameConfigMock.constantsMock(unitToExitFactor = 0.4))

    val exit1 = new ExitUnit(new PlayerId(0), id0, id1)

    a[AssertionError] should be thrownBy {
      val updated = new Buildings(Map(id0 → b0)).applyExitUnits(List(exit1), config)
      updated(id0) // for compiler
    }
  }

  "applyExitUnits" should "change buildings" in {
    val b0 = BuildingTest.building(
      id0,
      population = 100.1
    )

    val b1 = BuildingTest.building(
      id1,
      population = 200.2
    )

    val b2 = BuildingTest.building(
      id2,
      population = 300.3
    )

    val exit1 = new ExitUnit(new PlayerId(0), id0, id1)
    val exit3 = new ExitUnit(new PlayerId(0), id2, id1)

    val config = GameConfigMock.gameConfig(constants = GameConfigMock.constantsMock(unitToExitFactor = 0.5))

    val updated = new Buildings(Map(id0 → b0, id1 → b1, id2 → b2)).applyExitUnits(List(exit1, exit3), config)

    updated(id0).population should be(config.buildingAfterUnitToExit(b0.population))
    updated(id1).population should be(200.2)
    updated(id2).population should be(config.buildingAfterUnitToExit(b2.population))
  }

  "applyEnterUnits" should "work with empty list" in {
    val b0 = BuildingTest.building(
      id0,
      population = 100.1
    )

    val b1 = BuildingTest.building(
      id1,
      population = 200.2
    )

    val b2 = BuildingTest.building(
      id2,
      population = 300.3
    )

    val config = GameConfigMock.gameConfig(constants = GameConfigMock.constantsMock())

    val updated = new Buildings(Map(id0 → b0, id1 → b1, id2 → b2)).applyEnterUnits(List.empty, config, PlayerStateTest.playerStates)

    updated(id0).population should be(100.1)
    updated(id1).population should be(200.2)
    updated(id2).population should be(300.3)
  }

  "applyEnterUnits" should "increase friendly population" in {
    val b0 = BuildingTest.building(
      id0,
      population = 100.1
    )

    val b1 = BuildingTest.building(
      id1,
      population = 200.2,
      owner = Some(new PlayerId(1))
    )

    val b2 = BuildingTest.building(
      id2,
      population = 300.3
    )

    val unit = GameUnitTest.unit(count = 123, targetBuildingId = id1, owner = new PlayerId(1))
    val enter = EnterUnit(unit)

    val config = GameConfigMock.gameConfig(constants = GameConfigMock.constantsMock())

    val updated = new Buildings(Map(id0 → b0, id1 → b1, id2 → b2)).applyEnterUnits(List(enter), config, PlayerStateTest.playerStates)

    updated(id0).population should be(100.1)
    updated(id1).population should be(config.populationAfterFriendlyUnitEnter(b1.population, unit.count))
    updated(id2).population should be(300.3)
  }

  "applyEnterUnits" should "decrease enemy population if not capture" in {
    val b0 = BuildingTest.building(
      id0,
      population = 100.1
    )

    val b1 = BuildingTest.building(
      id1,
      population = 200.2,
      owner = Some(new PlayerId(1))
    )

    val b2 = BuildingTest.building(
      id2,
      population = 300.3
    )

    val unit = GameUnitTest.unit(count = 3, targetBuildingId = id1, owner = new PlayerId(0))
    val enter = EnterUnit(unit)

    val config = GameConfigMock.gameConfig(constants = GameConfigMock.constantsMock())

    val updated = new Buildings(Map(id0 → b0, id1 → b1, id2 → b2)).applyEnterUnits(List(enter), config, PlayerStateTest.playerStates)

    updated(id0).population should be(100.1)
    val playerStates = PlayerStateTest.playerStates.states
    updated(id1).population should be(config.buildingAfterEnemyUnitEnter(
      b1,
      unit,
      buildingPlayer = Some(playerStates(new PlayerId(1))),
      unitPlayer = playerStates(new PlayerId(0)))._1)
    updated(id1).owner should be(b1.owner)
    updated(id2).population should be(300.3)
  }

  "applyEnterUnits" should "change owner if capture" in {
    val b0 = BuildingTest.building(
      id0,
      population = 100.1
    )

    val b1 = BuildingTest.building(
      id1,
      population = 200.2,
      owner = Some(new PlayerId(1))
    )

    val b2 = BuildingTest.building(
      id2,
      population = 300.3
    )

    val unit = GameUnitTest.unit(count = 500, targetBuildingId = id1, owner = new PlayerId(0))
    val enter = EnterUnit(unit)

    val config = GameConfigMock.gameConfig()

    val updated = new Buildings(Map(id0 → b0, id1 → b1, id2 → b2)).applyEnterUnits(List(enter), config, PlayerStateTest.playerStates)

    updated(id0).population should be(100.1)
    val playerStates = PlayerStateTest.playerStates.states
    updated(id1).population should be(config.buildingAfterEnemyUnitEnter(
      b1,
      unit,
      buildingPlayer = Some(playerStates(new PlayerId(1))),
      unitPlayer = playerStates(new PlayerId(0)))._1)
    updated(id1).owner should be(Some(new PlayerId(0)))
    updated(id2).population should be(300.3)
  }

  "applyStrengtheningCasts" should "change buildings" in {
    val cast2 = new PlayerId(0) → id1

    val updated = new Buildings(Map(id0 → b0, id1 → b1, id2 → b2)).applyStrengtheningCasts(Map(cast2), 12345)

    updated(id0).strengthened should be(false)

    updated(id1).strengthened should be(true)
    updated(id1).strengtheningStartTime should be(12345)

    updated(id2).strengthened should be(false)
  }

  "cleanupStrengthening" should "not change buildings" in {
    val b0 = BuildingTest.building(id0, strengthened = false, strengtheningStartTime = 10000)
    val b1 = BuildingTest.building(id1, strengthened = true, strengtheningStartTime = 0)
    val b2 = BuildingTest.building(id2, strengthened = true, strengtheningStartTime = 10000)
    val config = GameConfigMock.gameConfig(strengthening = GameConfigMock.strengtheningMock(duration = 10000))

    val updated = new Buildings(Map(id0 → b0, id1 → b1, id2 → b2)).cleanupStrengthening(10000, config, PlayerStateTest.playerStates)
    updated(id0).strengthened should be(false)
    updated(id1).strengthened should be(false)
    updated(id2).strengthened should be(true)
  }

  "cleanupStrengthening" should "change buildings" in {
    val b0 = BuildingTest.building(id0, strengthened = false, strengtheningStartTime = 10000)
    val b1 = BuildingTest.building(id1, strengthened = true, strengtheningStartTime = 0)
    val b2 = BuildingTest.building(id2, strengthened = true, strengtheningStartTime = 10000)
    val config = GameConfigMock.gameConfig(strengthening = GameConfigMock.strengtheningMock(duration = 10000))

    val updated = new Buildings(Map(id0 → b0, id1 → b1, id2 → b2)).cleanupStrengthening(10001, config, PlayerStateTest.playerStates)
    updated(id0).strengthened should be(false)
    updated(id1).strengthened should be(false)
    updated(id2).strengthened should be(true)
  }

  "canShoot" should "return buildings" in {
    val b0 = BuildingTest.building(id0, lastShootTime = 10000)
    val b1 = BuildingTest.building(id1, lastShootTime = 0)
    val b2 = BuildingTest.building(id2, lastShootTime = 10000)
    val config = GameConfigMock.gameConfig(shooting = GameConfigMock.shootingMock(shootInterval = 10000))

    new Buildings(Map(id0 → b0, id1 → b1, id2 → b2)).canShoot(10000, config, PlayerStateTest.playerStates).size should be(0)

    val list = new Buildings(Map(id0 → b0, id1 → b1, id2 → b2)).canShoot(10001, config, PlayerStateTest.playerStates).toList
    list.size should be(1)
    list(0) should be(b1)
  }

  "applyShots" should "not change buildings" in {
    val b = new Buildings(Map(id0 → b0.shoot(1), id1 → b1.shoot(2), id2 → b2.shoot(3)))
    val updated = b.applyShots(1234, List.empty)
    updated(id0).lastShootTime should be(1)
    updated(id1).lastShootTime should be(2)
    updated(id2).lastShootTime should be(3)
  }

  "applyShots" should "change buildings" in {
    val b = new Buildings(Map(id0 → b0.shoot(1), id1 → b1.shoot(2), id2 → b2.shoot(3)))
    val bullet0 = new Bullet(b0, GameUnitTest.unit(), 1000, 2000)
    val bullet2 = new Bullet(b2, GameUnitTest.unit(), 1000, 2000)
    val updated = b.applyShots(1234, List(bullet0, bullet2))
    updated(id0).lastShootTime should be(1234)
    updated(id1).lastShootTime should be(2)
    updated(id2).lastShootTime should be(1234)
  }

  "dto" should "be correct" in {
    val dto = new Buildings(Map(id0 → b0, id1 → b1, id2 → b2)).dto.toList
    dto.size should be(3)
    dto(0).getId.getId should be(0)
    dto(1).getId.getId should be(1)
    dto(2).getId.getId should be(2)
  }

  "dto" should "work with empty list" in {
    val dto = new GameUnits(List.empty).dto(1200)
    dto.size should be(0)
  }

  "updateDto" should "be correct" in {
    val dto = new Buildings(Map(id0 → b0, id1 → b1, id2 → b2)).updateDto.toList
    dto.size should be(3)
    dto(0).getId.getId should be(0)
    dto(1).getId.getId should be(1)
    dto(2).getId.getId should be(2)
  }

  "updateDto" should "work with empty list" in {
    val dto = new GameUnits(List.empty).updateDto(1200)
    dto.size should be(0)
  }

  "getUpdateMessages" should "not update new buildings" in {
    val list = Buildings.getUpdateMessages(Map.empty, Map(id0 → b0))
    list.size should be(0)
  }
  "getUpdateMessages" should "update changed buildings" in {
    val list = Buildings.getUpdateMessages(
      Map(id0 → b0, id1 → b1, id2 → b2),
      Map(id0 → b0.setPopulation(b0.population + 1), id1 → b1, id2 → b2.setPopulation(b2.population + 1))).toList
    list.size should be(2)
    list(0).buildingUpdateDTO.getId.getId should be(b0.id.id)
    list(1).buildingUpdateDTO.getId.getId should be(b2.id.id)
  }
}

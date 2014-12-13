package ru.rknrl.castles.game.objects.assistance

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.game._
import ru.rknrl.castles.game.objects.buildings.{BuildingId, BuildingTest, Buildings}
import ru.rknrl.castles.game.objects.players.PlayerId
import ru.rknrl.castles.game.objects.{Assistance, PlayerStateTest}
import ru.rknrl.utils.UnitIdIterator

class AssistanceTest extends FlatSpec with Matchers {
  val playerId0 = new PlayerId(0)
  val playerId1 = new PlayerId(1)

  private val id0 = new BuildingId(0)
  private val b0 = BuildingTest.building(id = id0, owner = Some(playerId0))
  private val id1 = new BuildingId(1)
  private val b1 = BuildingTest.building(id = id1, owner = Some(playerId1))
  private val buildings = new Buildings(Map(id0 → b0, id1 → b1))

  private val config = GameConfigMock.gameConfig(
    assistance = GameConfigMock.assistanceMock(count = 66)
  )

  private val playerStates = PlayerStateTest.playerStates

  private val time = 1990

  "casts→units" should "be work with empty list" in {
    val unitIdIterator = new UnitIdIterator
    unitIdIterator.next
    unitIdIterator.next

    val units = Assistance.`casts→units`(Map.empty, buildings, config, playerStates, unitIdIterator, time)
    units.size should be(0)
  }

  "casts→units" should "create unit" in {
    val unitIdIterator = new UnitIdIterator
    unitIdIterator.next
    unitIdIterator.next

    val cast = playerId0 → id0

    val units = Assistance.`casts→units`(Map(cast), buildings, config, playerStates, unitIdIterator, time)
    units.size should be(1)
    val unit = units.head

    unit.id.id should be(3)
    unit.buildingPrototype should be(config.assistanceBuildingPrototype)
    unit.count should be(config.assistanceCount(playerStates.states(playerId0)))
    // todo unit.startPos should be()
    //    todo unit.endPos should be()
    unit.startTime should be(1990)
    unit.speed should be(config.getUnitSpeed(config.assistanceBuildingPrototype, playerStates.states(playerId0), strengthened = false))
    unit.targetBuildingId should be(id0)
    unit.owner should be(playerId0)
    unit.strengthened should be(false)
  }

  "casts→units" should "create units" in {
    val unitIdIterator = new UnitIdIterator

    val cast1 = playerId0 → id0
    val cast2 = playerId1 → id1

    val units = Assistance.`casts→units`(Map(cast1, cast2), buildings, config, playerStates, unitIdIterator, time)
    units.size should be(2)
  }

  "casts→units" should "throw AssertionError if building.owner != cast owner" in {
    val unitIdIterator = new UnitIdIterator

    val cast1 = playerId0 → id1
    val cast2 = playerId1 → id0

    a[AssertionError] should be thrownBy {
      val units = Assistance.`casts→units`(Map(cast1, cast2), buildings, config, playerStates, unitIdIterator, time)
    }
  }
}
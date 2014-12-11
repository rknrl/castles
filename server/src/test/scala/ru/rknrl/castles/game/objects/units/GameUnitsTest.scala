package ru.rknrl.castles.game.objects.units

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.account.objects.BuildingPrototype
import ru.rknrl.castles.game.objects.buildings.BuildingId
import ru.rknrl.castles.game.objects.players.PlayerId
import ru.rknrl.castles.rmi.RemoveUnitMsg
import ru.rknrl.dto.CommonDTO.{BuildingLevel, BuildingType}
import ru.rknrl.utils.Point

class GameUnitsTest extends FlatSpec with Matchers {
  private val unit0 = new GameUnit(
    new UnitId(0),
    new BuildingPrototype(BuildingType.HOUSE, BuildingLevel.LEVEL_1),
    count = 23,
    startPos = new Point(0.1, 0.2),
    endPos = new Point(1.5, 1.5),
    startTime = 800,
    speed = 0.006,
    targetBuildingId = new BuildingId(0),
    new PlayerId(0),
    strengthened = false
  )

  private val unit1 = new GameUnit(
    new UnitId(1),
    new BuildingPrototype(BuildingType.HOUSE, BuildingLevel.LEVEL_1),
    count = 23,
    startPos = new Point(0.1, 0.2),
    endPos = new Point(0.5, 0.5),
    startTime = 1000,
    speed = 0.006,
    targetBuildingId = new BuildingId(0),
    new PlayerId(0),
    strengthened = false
  )

  private val unit2 = new GameUnit(
    new UnitId(2),
    new BuildingPrototype(BuildingType.HOUSE, BuildingLevel.LEVEL_1),
    count = 23,
    startPos = new Point(0.1, 0.2),
    endPos = new Point(2.5, 2.5),
    startTime = 900,
    speed = 0.006,
    targetBuildingId = new BuildingId(0),
    new PlayerId(0),
    strengthened = false
  )

  "add" should "change list" in {
    val b = new GameUnits(List(unit0)).add(List(unit1, unit2))
    val list = b.units.toList
    list.size should be(3)
    list(0) should be(unit0)
    list(1) should be(unit1)
    list(2) should be(unit2)
  }

  "applyRemoveMessages" should "work with empty units" in {
    val b = new GameUnits(List.empty).applyRemoveMessages(List.empty)
    b.units.size should be(0)
  }

  "applyRemoveMessages" should "work with empty list" in {
    val b = new GameUnits(List(unit0, unit1, unit2)).applyRemoveMessages(List.empty)
    b.units.size should be(3)
  }

  "applyRemoveMessages" should "remove units" in {
    val msg = new RemoveUnitMsg(unit1.id.dto)
    val b = new GameUnits(List(unit0, unit1, unit2)).applyRemoveMessages(List(msg))
    val list = b.units.toList
    list.size should be(2)
    list(0) should be(unit0)
    list(1) should be(unit2)
  }

  "dto" should "be correct" in {
    val dto = new GameUnits(List(unit0, unit1, unit2)).dto(1200).toList
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
    val dto = new GameUnits(List(unit0, unit1, unit2)).updateDto(1200).toList
    dto.size should be(3)
    dto(0).getId.getId should be(0)
    dto(1).getId.getId should be(1)
    dto(2).getId.getId should be(2)
  }

  "updateDto" should "work with empty list" in {
    val dto = new GameUnits(List.empty).updateDto(1200)
    dto.size should be(0)
  }

  "units->addMessages" should "work with empty list" in {
    GameUnits.`units→addMessages`(List.empty, 423).size should be(0)
  }

  "units->addMessages" should "return messages" in {
    val addMessages = GameUnits.`units→addMessages`(List(unit0, unit1, unit2), 1200).toList

    addMessages.size should be(3)

    addMessages(0).unitDTO.getId.getId should be(0)
    addMessages(1).unitDTO.getId.getId should be(1)
    addMessages(2).unitDTO.getId.getId should be(2)
  }

  "getUpdateMessages" should "not update new units" in {
    val list = GameUnits.getUpdateMessages(List.empty, List(unit0), 1200)
    list.size should be(0)
  }
  "getUpdateMessages" should "update changed units" in {
    val list = GameUnits.getUpdateMessages(List(unit0, unit1, unit2), List(unit0.setCount(9), unit1, unit2.setCount(13)), 1200).toList
    list.size should be(2)
    list(0).unitUpdateDTO.getId.getId should be(unit0.id.id)
    list(1).unitUpdateDTO.getId.getId should be(unit2.id.id)
  }
}

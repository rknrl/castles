package ru.rknrl.castles.game.objects.units

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.account.objects.BuildingPrototype
import ru.rknrl.castles.game.objects.buildings.BuildingId
import ru.rknrl.castles.game.objects.players.PlayerId
import ru.rknrl.dto.CommonDTO.{BuildingLevel, BuildingType}
import ru.rknrl.utils.Point

object GameUnitTest {
  def owner = new PlayerId(1)

  def unit(id: UnitId = new UnitId(8),
           buildingPrototype: BuildingPrototype = new BuildingPrototype(BuildingType.CHURCH, BuildingLevel.LEVEL_3),
           count: Int = 123,
           startPos: Point = new Point(0.44, 0.55),
           endPos: Point = new Point(0.77, 0.88),
           startTime: Long = 65,
           speed: Double = 0.05,
           targetBuildingId: BuildingId = new BuildingId(2),
           owner: PlayerId = owner,
           strengthened: Boolean = true) =
    new GameUnit(
      id = id,
      buildingPrototype = buildingPrototype,
      count = count,
      startPos = startPos,
      endPos = endPos,
      startTime = startTime,
      speed = speed,
      targetBuildingId = targetBuildingId,
      owner = owner,
      strengthened = strengthened)
}

class GameUnitTest extends FlatSpec with Matchers {

  import ru.rknrl.castles.game.objects.units.GameUnitTest._

  "setCount" should "change count value & don't change other values" in {
    val a = unit(count = 2)
    val b = a.setCount(33)
    b.count should be(33)

    b.id should be(a.id)
    b.buildingPrototype should be(a.buildingPrototype)
    b.startPos should be(a.startPos)
    b.endPos should be(a.endPos)
    b.startTime should be(a.startTime)
    b.speed should be(a.speed)
    b.targetBuildingId should be(a.targetBuildingId)
    b.owner should be(a.owner)
    b.strengthened should be(a.strengthened)
  }

  "differentWith" should "be false with same units" in {
    val u1 = unit(count = 123, speed = 0.14)
    val u2 = unit(count = 123, speed = 0.14)
    u1 differentWith u2 should be(false)
  }

  "differentWith" should "be true when units are not equals" in {
    unit(count = 124, speed = 0.14) differentWith unit(count = 123, speed = 0.14) should be(true)
    unit(count = 124, speed = 0.14) differentWith unit(count = 124, speed = 0.15) should be(true)
    unit(count = 124, speed = 0.14) differentWith unit(count = 1, speed = 1) should be(true)
  }

  "dto" should "be correct" in {
    val time = 20
    val u: GameUnit = unit(startTime = 10)
    val dto = u.dto(time)

    dto.getId.getId should be(u.id.id)
    dto.getType should be(u.buildingPrototype.buildingType)
    dto.getCount should be(u.count)
    dto.getPos.getX should be(u.getPos(time).x.toFloat)
    dto.getPos.getY should be(u.getPos(time).y.toFloat)
    dto.getSpeed should be(u.speed.toFloat)
    dto.getTargetBuildingId.getId should be(u.targetBuildingId.id)
    dto.getOwner.getId should be(u.owner.id)
    dto.getStrengthened should be(u.strengthened)
  }

  "updateDto" should "be correct" in {
    val time = 20
    val u: GameUnit = unit(startTime = 10)
    val dto = u.updateDto(time)

    dto.getId.getId should be(u.id.id)
    dto.getPos.getX should be(u.getPos(time).x.toFloat)
    dto.getPos.getY should be(u.getPos(time).y.toFloat)
    dto.getSpeed should be(u.speed.toFloat)
    dto.getCount should be(u.count)
  }
}

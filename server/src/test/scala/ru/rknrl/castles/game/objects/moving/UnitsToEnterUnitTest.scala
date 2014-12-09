package ru.rknrl.castles.game.objects.moving

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.game.objects.Moving
import ru.rknrl.castles.game.objects.units.{GameUnitTest, UnitId}
import ru.rknrl.utils.Point

class UnitsToEnterUnitTest extends FlatSpec with Matchers {
  "units->EnterUnits" should "work with empty list" in {
    Moving.`units→enterUnit`(List.empty, 1990).size should be(0)
  }

  "units->EnterUnits" should "return messages" in {
    val unit0 = GameUnitTest.unit(
      id = new UnitId(0),
      startPos = new Point(0, 0),
      endPos = new Point(1, 0),
      startTime = 1990,
      speed = 1
    )
    val unit1 = GameUnitTest.unit(
      id = new UnitId(1),
      startPos = new Point(0, 0),
      endPos = new Point(1, 1),
      startTime = 1990,
      speed = 1
    )

    val unit2 = GameUnitTest.unit(
      id = new UnitId(2),
      startPos = new Point(0, 0),
      endPos = new Point(1, 0),
      startTime = 1990,
      speed = 0.1
    )

    val enterUnits = Moving.`units→enterUnit`(List(unit0, unit1, unit2), 1991).toList

    enterUnits.size should be(1)

    enterUnits(0).unit.id.id should be(0)
  }
}

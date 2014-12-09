package ru.rknrl.castles.game.objects.moving

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.game.objects.Moving
import ru.rknrl.castles.game.objects.Moving.EnterUnit
import ru.rknrl.castles.game.objects.units.{GameUnitTest, UnitId}

class EnterUnitToRemoveUnitMsgTest extends FlatSpec with Matchers {
  "enterUnit->removeUnitMsg" should "work with empty list" in {
    Moving.`enterUnit→removeUnitMsg`(List.empty).size should be(0)
  }

  "enterUnit->removeUnitMsg" should "return messages" in {
    val enter0 = new EnterUnit(GameUnitTest.unit(new UnitId(0)))
    val enter1 = new EnterUnit(GameUnitTest.unit(new UnitId(1)))
    val enter2 = new EnterUnit(GameUnitTest.unit(new UnitId(2)))

    val messages = Moving.`enterUnit→removeUnitMsg`(List(enter0, enter1, enter2)).toList
    messages.size should be(3)
    messages(0).unitIdDTO.getId should be(0)
    messages(1).unitIdDTO.getId should be(1)
    messages(2).unitIdDTO.getId should be(2)
  }
}

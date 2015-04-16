//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import org.scalatest.{Matchers, WordSpec}
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.core.points.Point
import ru.rknrl.dto.UnitId

// todo: dto, updateDto
class GameUnitTest extends WordSpec with Matchers {

  "negative count" in {
    a[Exception] shouldBe thrownBy {
      unitMock(count = -1)
    }

    unitMock(count = 0) // ok
  }

  "copy equals" in {
    val u = unitMock()
    checkUnit(u.copy(), u)
  }

  "copy" in {
    val u = unitMock(count = 1.1)
    checkUnit(u.copy(newCount = 2.2), unitMock(count = 2.2))
  }

  "applyDamagers" should {
    "выбирает дамагеров, в радиус которых попадает юнит" +
      "уменьшает count на их дамаг" +
      "не меняет ничего больше" in {

      val a = TDamager(Point(2.1, 2.2), damagerConfigMock(radius = 0.4, powerVsUnit = 1.2))
      val b = TDamager(Point(10.1, 20.2), damagerConfigMock(radius = 1.1, powerVsUnit = 3.3))
      val c = TDamager(Point(3.1, 1.1), damagerConfigMock(radius = 1.5, powerVsUnit = 2.2))

      val damagers = List(a, b, c)

      val oldUnit = unitMock(
        count = 14,
        fromBuilding = buildingMock(
          pos = Point(2, 2),
          buildingStat = statMock(defence = 1.3)
        ),
        toBuilding = buildingMock(pos = Point(2, 2))
      )

      val newUnit = oldUnit.applyDamagers(damagers, time = 0)

      // По радиусу подходят только A и C
      // (14 - 1.2 / 1.3) - 2.2 / 1.3 = 11.38

      checkUnit(newUnit, oldUnit.copy(newCount = 11.38))
    }
  }

  "applyBullets" should {
    "выбирает пули, которые относятся к этому юниту" +
      "уменьшает count на их дамаг" +
      "не меняет ничего больше" in {

      val bullets = List(
        bulletMock(unit = unitMock(id = UnitId(1)), powerVsUnit = 1.2),
        bulletMock(unit = unitMock(id = UnitId(2)), powerVsUnit = 3.3),
        bulletMock(unit = unitMock(id = UnitId(1)), powerVsUnit = 2.2)
      )

      val oldUnit = unitMock(
        id = UnitId(1),
        count = 13,
        fromBuilding = buildingMock(
          buildingStat = statMock(defence = 1.3)
        )
      )

      val newUnit = oldUnit.applyBullets(bullets)

      // вторая пуля не походят по unitId
      // (13 - 1.2 / 1.3) - 2.2 / 1.3 = 10.38

      checkUnit(newUnit, oldUnit.copy(newCount = 10.38))
    }
  }

  "differentWith" should {
    "true, только если округленный count различается" in {

      unitMock(count = 7) differentWith unitMock(count = 8) shouldBe true
      unitMock(count = 7) differentWith unitMock(count = 7.4) shouldBe false

    }
  }

  "getUpdateMessages" should {
    "Возвращает апдейт мессадж" +
      "если юнит был и в старом и в новом списке" +
      "и они различаются по differentWith" in {

      val oldUnits = List(
        unitMock(id = UnitId(1), count = 11.2),
        unitMock(id = UnitId(2), count = 12.2)
      )

      val newUnits = List(
        unitMock(id = UnitId(1), count = 11.2),
        unitMock(id = UnitId(3), count = 7),
        unitMock(id = UnitId(2), count = 9)
      )

      val updateMessages = GameUnit.getUpdateMessages(oldUnits, newUnits)
      updateMessages should have size 1
      updateMessages.head.unitUpdate.id shouldBe UnitId(2)
      updateMessages.head.unitUpdate.count shouldBe 9

    }

  }


}

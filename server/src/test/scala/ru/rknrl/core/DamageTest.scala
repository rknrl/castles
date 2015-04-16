//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.core

import org.scalatest.{Matchers, WordSpec}
import ru.rknrl.castles.game.DamagerConfig
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.core.points.Point

class DamageTest extends WordSpec with Matchers {

  "DamagerConfig.bonused" should {
    "Увеличивает powerVsUnit и powerVsBuilding на нужное значение" +
      "не меняет остальные параметры" in {

      val config = DamagerConfig(
        powerVsUnit = 1.1,
        powerVsBuilding = 2.1,
        maxPowerBonus = 0.8,
        radius = 1
      )

      config.bonused(0).powerVsUnit shouldBe 1.1
      config.bonused(0).powerVsBuilding shouldBe 2.1
      config.bonused(0).maxPowerBonus shouldBe 0.8
      config.bonused(0).radius shouldBe 1

      config.bonused(0.5).powerVsUnit shouldBe 1.5
      config.bonused(0.5).powerVsBuilding shouldBe 2.5
      config.bonused(0.5).maxPowerBonus shouldBe 0.8
      config.bonused(0.5).radius shouldBe 1

      config.bonused(1).powerVsUnit shouldBe (1.9 +- 0.000001)
      config.bonused(1).powerVsBuilding shouldBe (2.9 +- 0.000001)
      config.bonused(1).maxPowerBonus shouldBe 0.8
      config.bonused(1).radius shouldBe 1
    }
  }

  "Damaged.floorCount" should {
    "округляет в меньшую сторону" in {
      damagedMock(count = 7.77).floorCount shouldBe 7
    }
  }

  "inRadius" in {
    val a = TDamager(Point(2.1, 2.2), damagerConfigMock(radius = 0.4))
    val b = TDamager(Point(10.1, 20.2), damagerConfigMock(radius = 1.1))
    val c = TDamager(Point(3.1, 1.1), damagerConfigMock(radius = 1.5))

    val damagers = List(a, b, c)

    val damagersInRadius = Damage.inRadius(damagers, damagedMock(pos = Point(2, 2)), time = 0)

    damagersInRadius should have size 2
    damagersInRadius should contain(a)
    damagersInRadius should not contain b
    damagersInRadius should contain(c)
  }

  "applyDamage" should {
    "Уменьшает count на нужное значение" in {

      val newDamaged = Damage.applyDamage(
        damaged = damagedMock(count = 10, stat = statMock(defence = 1.3)),
        powers = List(1.1, 2.2)
      )

      // (10 - 1.1 / 1.3) - 2.2 / 1.3 = 7.46

      newDamaged.count shouldBe (7.46 +- 0.01)
    }

    "не уходит в минус" in {

      val newDamaged = Damage.applyDamage(damagedMock(count = 10), List(999))
      newDamaged.count shouldBe 0

    }
  }
}
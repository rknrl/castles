package ru.rknrl.castles.game.objects.bullets

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.game.objects.buildings.{Building, BuildingId, BuildingTest}
import ru.rknrl.castles.game.objects.units.{GameUnit, GameUnitTest, UnitId}

object BulletTest {
  def bullet(building: Building = BuildingTest.building(id = new BuildingId(7)),
             unit: GameUnit = GameUnitTest.unit(id = new UnitId(88)),
             startTime: Long = 50,
             duration: Long = 100) =
    new Bullet(
      building = building,
      unit = unit,
      startTime = startTime,
      duration = duration
    )
}

class BulletTest extends FlatSpec with Matchers {
  "dto" should "be correct" in {
    val dto = BulletTest.bullet().dto(60)

    dto.getBuildingId.getId should be(7)
    dto.getUnitId.getId should be(88)
    dto.getDuration should be(100)
  }
}

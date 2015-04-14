//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import org.scalatest.{FreeSpec, Matchers}
import ru.rknrl.castles.game.state.Assistance.castToUnit
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.dto.BuildingLevel.{LEVEL_1, LEVEL_2}
import ru.rknrl.dto.BuildingType.{HOUSE, TOWER}
import ru.rknrl.dto.{BuildingId, BuildingPrototype, PlayerId, UnitId}

class AssistanceTest extends FreeSpec with Matchers {

  "castToUnit" - {

    val b =
      buildingMock(
        id = BuildingId(6),
        pos = Point(50, 70),
        buildingPrototype = BuildingPrototype(TOWER, LEVEL_2)
      )

    val unitIdIterator = new UnitIdIterator
    unitIdIterator.next
    unitIdIterator.next

    val player2 = playerMock(
      id = PlayerId(2),
      stat = Stat(1.1, 2.1, 3.1)
    )

    val unit = castToUnit(
      cast = player2 → BuildingId(6),

      buildings = List(b),

      config = gameConfigMock(
        units = unitsConfigMock(
          house = Stat(0.3, 0.5, 0.2)
        ),
        buildings = buildingsConfigMock(
          tower2 = buildingConfigMock(
            maxCount = 30
          )
        ),
        assistance = assistanceConfigMock(
          power = 0.6,
          maxBonusPower = 0.4
        )
      ),

      churchesProportion = new ChurchesProportion(Map(
        PlayerId(2) → 0.5
      )),

      unitIdIterator = unitIdIterator,

      assistancePositions = Map(
        PlayerId(2) → Point(20, 30)
      ),

      time = 123
    )

    // count = maxPopulation * (assistance.power + bonus)
    // count = 30 * (0.6 + 0.4 * 0.5) = 24

    unit.id shouldBe UnitId(2)
    unit.count shouldBe 24
    unit.startTime shouldBe 123
    unit.buildingPrototype shouldBe BuildingPrototype(HOUSE, LEVEL_1)
    unit.owner shouldBe player2
    unit.strengthened shouldBe false
    unit.stat shouldBe Stat(0.3 * 1.1, 0.5 * 2.1, 0.2 * 3.1)
    unit.fromBuilding.id shouldBe BuildingId(-1)
    unit.fromBuilding.pos shouldBe Point(20, 30)
    unit.toBuilding shouldBe b
    // duration = distance / speed = 50 / 0.62 = 80
    unit.duration shouldBe 80
  }

}

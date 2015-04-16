//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import org.scalatest.{Matchers, WordSpec}
import ru.rknrl.castles.game.state.Moving._
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.core.Stat
import ru.rknrl.core.points.Point
import ru.rknrl.dto.{BuildingId, MoveDTO, PlayerId, UnitId}

class MovingTest extends WordSpec with Matchers {
  "unitsToExit" in {
    unitsToExit(10.1) shouldBe 5
    unitsToExit(20.7) shouldBe 10
  }

  "moveActionToExitUnit" in {
    val from = buildingMock(pos = Point(0, 0), buildingStat = Stat(1, 1, 0.2))
    val to = buildingMock(pos = Point(2, 0))
    val move = Move(
      playerId = PlayerId(0),
      fromBuilding = from,
      toBuilding = to
    )
    val unitIdIterator = new UnitIdIterator
    val u = moveActionToExitUnit(move, unitIdIterator, time = 1)
    u.id shouldBe UnitId(0)
    u.fromBuilding shouldBe from
    u.toBuilding shouldBe to
    u.count shouldBe unitsToExit(from.count)
    u.startTime shouldBe 1
    u.duration shouldBe 10 // duration = distance / speed = 2 / 0.2 = 10
  }

  "convert" in {
    val moveActions = Map(
      PlayerId(0) → MoveDTO(
        fromBuildings = List(BuildingId(1)),
        toBuilding = BuildingId(2)
      ),
      PlayerId(1) → MoveDTO(
        fromBuildings = List(BuildingId(3), BuildingId(4)),
        toBuilding = BuildingId(2)
      )
    )

    val b1 = buildingMock(id = BuildingId(1))
    val b2 = buildingMock(id = BuildingId(2))
    val b3 = buildingMock(id = BuildingId(3))
    val b4 = buildingMock(id = BuildingId(4))

    val buildings = List(b1, b2, b3, b4)

    val moves = convert(moveActions, buildings)
    moves shouldBe List(
      Move(PlayerId(0), fromBuilding = b1, toBuilding = b2),
      Move(PlayerId(1), fromBuilding = b3, toBuilding = b2),
      Move(PlayerId(1), fromBuilding = b4, toBuilding = b2)
    )
  }

  "moveActionsToExitUnits" should {
    "same id" in {
      val moveActions = Map(
        PlayerId(0) → MoveDTO(
          fromBuildings = List(BuildingId(1)),
          toBuilding = BuildingId(1)
        )
      )

      val b1 = buildingMock(
        id = BuildingId(1),
        owner = Some(playerMock(PlayerId(0))),
        count = 10
      )

      val buildings = List(b1)

      val units = moveActionsToExitUnits(moveActions, buildings, new UnitIdIterator, time = 1)
      units.size shouldBe 0
    }

    "not owner" in {
      val moveActions = Map(
        PlayerId(0) → MoveDTO(
          fromBuildings = List(BuildingId(1)),
          toBuilding = BuildingId(2)
        )
      )

      val b1 = buildingMock(
        id = BuildingId(1),
        owner = Some(playerMock(PlayerId(1))),
        count = 10
      )
      val b2 = buildingMock(id = BuildingId(2))

      val buildings = List(b1, b2)

      val units = moveActionsToExitUnits(moveActions, buildings, new UnitIdIterator, time = 1)
      units.size shouldBe 0
    }

    "not count" in {
      val moveActions = Map(
        PlayerId(0) → MoveDTO(
          fromBuildings = List(BuildingId(1)),
          toBuilding = BuildingId(2)
        )
      )

      val b1 = buildingMock(
        id = BuildingId(1),
        owner = Some(playerMock(PlayerId(0))),
        count = 1
      )
      val b2 = buildingMock(id = BuildingId(2))

      val buildings = List(b1, b2)

      val units = moveActionsToExitUnits(moveActions, buildings, new UnitIdIterator, time = 1)
      units.size shouldBe 0
    }

    "ok" in {
      val moveActions = Map(
        PlayerId(0) → MoveDTO(
          fromBuildings = List(BuildingId(1), BuildingId(3)),
          toBuilding = BuildingId(2)
        ),
        PlayerId(1) → MoveDTO(
          fromBuildings = List(BuildingId(3), BuildingId(4)),
          toBuilding = BuildingId(2)
        )
      )

      val b1 = buildingMock(
        id = BuildingId(1),
        owner = Some(playerMock(PlayerId(0))),
        count = 10,
        pos = Point(0, 0)
      )
      val b2 = buildingMock(
        id = BuildingId(2),
        pos = Point(1, 1)
      )
      val b3 = buildingMock(
        id = BuildingId(3),
        owner = Some(playerMock(PlayerId(1))),
        count = 10,
        pos = Point(2, 2)
      )
      val b4 = buildingMock(
        id = BuildingId(4),
        owner = Some(playerMock(PlayerId(1))),
        count = 10,
        pos = Point(3, 3)
      )

      val buildings = List(b1, b2, b3, b4)

      val units = moveActionsToExitUnits(moveActions, buildings, new UnitIdIterator, time = 1).toList
      units.size shouldBe 3
      val iterator = new UnitIdIterator
      checkUnit(units(0), moveActionToExitUnit(Move(PlayerId(0), fromBuilding = b1, toBuilding = b2), iterator, time = 1))
      checkUnit(units(1), moveActionToExitUnit(Move(PlayerId(1), fromBuilding = b3, toBuilding = b2), iterator, time = 1))
      checkUnit(units(2), moveActionToExitUnit(Move(PlayerId(2), fromBuilding = b4, toBuilding = b2), iterator, time = 1))
    }
  }
}

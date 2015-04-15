//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import org.scalatest.{Matchers, WordSpec}
import ru.rknrl.castles.game.state.Bullets._
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.dto.BuildingLevel.LEVEL_3
import ru.rknrl.dto.{PlayerId, BuildingPrototype}
import ru.rknrl.dto.BuildingType.TOWER

class BulletsTest extends WordSpec with Matchers {

  "getHeadsIfExists" in {
    val xs = List(
      List(),
      List(1, 2, 3, 4),
      List(),
      List(),
      List(22, 33, 44),
      List(7)
    )

    Bullets.heads(xs) shouldBe List(1, 22, 7)
  }

  "createBullets" in {
    val b = buildingMock(
      pos = Point(2, 2),
      buildingPrototype = BuildingPrototype(TOWER, LEVEL_3)
    )

    val u0 = unitMock(
      fromBuilding = buildingMock(pos = Point(2, 4)),
      toBuilding = b,
      startTime = 0,
      duration = 10
    )
    val u1 = unitMock(
      fromBuilding = buildingMock(pos = Point(2, 4)),
      toBuilding = b,
      startTime = 0,
      duration = 10
    )
    val u2 = unitMock(
      fromBuilding = buildingMock(pos = Point(3, 2)),
      toBuilding = b,
      startTime = 0,
      duration = 7
    )

    val config = gameConfigMock(
      buildings = buildingsConfigMock(
        tower3 = buildingConfigMock(
          shotPower = Some(3.14)
        )
      ),
      shooting = shootingConfigMock(
        shootRadius = 1.1,
        speed = 0.25
      )
    )

    createBullets(List(b), List(u0, u1, u2), time = 5, config) shouldBe
      List(
        createBullet(b, u0, time = 5, config)
      )
  }


  "canCreateBullet" in {
    val b = buildingMock(pos = Point(2, 2), owner = Some(playerMock(PlayerId(1))))

    val u0 = unitMock(
      fromBuilding = buildingMock(pos = Point(2, 4), owner = Some(playerMock(PlayerId(0)))),
      toBuilding = b,
      startTime = 0,
      duration = 10
    )
    val u1 = unitMock(
      fromBuilding = buildingMock(pos = Point(3, 2),owner = Some(playerMock(PlayerId(0)))),
      toBuilding = b,
      startTime = 0,
      duration = 2
    )
    val u3 = unitMock(
      fromBuilding = buildingMock(pos = Point(3, 2), owner = Some(playerMock(PlayerId(1)))),
      toBuilding = b,
      startTime = 0,
      duration = 10
    )

    val u4 = unitMock(
      fromBuilding = buildingMock(pos = Point(2, 2), owner = Some(playerMock(PlayerId(0)))),
      toBuilding = b,
      startTime = 0,
      duration = 10
    )

    val config = gameConfigMock(
      shooting = shootingConfigMock(
        shootRadius = 1.1,
        speed = 0.5
      )
    )

    canCreateBullet(b, u0, time = 4, config) shouldBe false // не в радиусе
    canCreateBullet(b, u0, time = 5, config) shouldBe true  // в радиусе
    canCreateBullet(b, u1, time = 1, config) shouldBe false // в радиусе но успеет дойти до дома

    canCreateBullet(b, u3, time = 5, config) shouldBe false // because same owner
    canCreateBullet(b, u4, time = 5, config) shouldBe false // because same positions, distance == 0
  }

  "createBullet" in {
    val b = buildingMock(
      buildingPrototype = BuildingPrototype(TOWER, LEVEL_3),
      pos = Point(1, 1)
    )

    val u = unitMock(
      fromBuilding = buildingMock(
        pos = Point(2, 2)
      ),
      toBuilding = buildingMock(
        pos = Point(1, 1)
      ),
      startTime = 1,
      duration = 10
    )

    val bullet = createBullet(
      b = b,
      u = u,
      time = 1,
      config = gameConfigMock(
        buildings = buildingsConfigMock(
          tower3 = buildingConfigMock(
            shotPower = Some(3.14)
          )
        ),
        shooting = shootingConfigMock(
          speed = 0.4
        )
      )
    )

    bullet.building shouldBe b
    bullet.unit shouldBe u
    bullet.startTime shouldBe 1
    bullet.duration shouldBe (Math.sqrt(1 + 1) / 0.4).toLong
    bullet.powerVsUnit shouldBe 3.14
  }

}

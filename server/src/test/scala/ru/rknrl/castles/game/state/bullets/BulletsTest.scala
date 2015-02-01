package ru.rknrl.castles.game.state.bullets

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.game.state.PlayerStateTest
import ru.rknrl.castles.game.state.buildings.{BuildingId, BuildingTest, Buildings}
import ru.rknrl.castles.game.state.units.{GameUnitTest, GameUnits}
import ru.rknrl.castles.mock.GameConfigMock
import ru.rknrl.utils.Point

class BulletsTest extends FlatSpec with Matchers {

  "getNearestUnits" should "return units" in {
    val buildingPos = new Point(0, 0)
    val building = BuildingTest.building(x = 0, y = 0)
    val config = GameConfigMock.gameConfig(shooting = GameConfigMock.shootingMock(shootRadius = 3))

    val unit1 = GameUnitTest.unit(
      startPos = new Point(1, 1),
      startTime = 1990
    )
    val unit2 = GameUnitTest.unit(
      startPos = new Point(0, 0),
      startTime = 1990
    )
    val unit3 = GameUnitTest.unit(
      startPos = new Point(10, 20),
      startTime = 1990
    )
    val units = new GameUnits(List(unit1, unit2, unit3))
    val shootRadius = 3
    val nearest = Bullets.getNearestUnits(buildingPos, units, 1990, config, shootRadius).toList
    nearest.size should be(2)
    nearest(0) should be(unit1)
    nearest(1) should be(unit2)
  }

  "getNearestUnits" should "not return units" in {
    val buildingPos = new Point(0, 0)

    val building = BuildingTest.building(x = 0, y = 0)

    val config = GameConfigMock.gameConfig(shooting = GameConfigMock.shootingMock(shootRadius = 3))

    val unit1 = GameUnitTest.unit(
      startPos = new Point(3, 3),
      startTime = 1990
    )
    val unit2 = GameUnitTest.unit(
      startPos = new Point(-10, -10),
      startTime = 1990
    )
    val unit3 = GameUnitTest.unit(
      startPos = new Point(10, 20),
      startTime = 1990
    )
    val units = new GameUnits(List(unit1, unit2, unit3))
    val shootRadius = 3
    val nearest = Bullets.getNearestUnits(buildingPos, units, 1990, config, shootRadius).toList
    nearest.size should be(0)
  }

  "option→bullets" should "work with empty list" in {
    Bullets.`option→bullets`(List.empty).size should be(0)
  }

  "option→bullets" should "work with None" in {
    Bullets.`option→bullets`(List(None)).size should be(0)
  }

  "option→bullets" should "work with Some" in {
    Bullets.`option→bullets`(List(Some(BulletTest.bullet()))).size should be(1)
  }

  "option→bullets" should "work with Some&None" in {
    Bullets.`option→bullets`(List(
      Some(BulletTest.bullet()),
      None,
      Some(BulletTest.bullet())
    )).size should be(2)
  }

  "createOptionBullet" should "be work with empty list" in {
    val id = new BuildingId(1)
    val b = BuildingTest.building(id = id, x = 0, y = 0, lastShootTime = 0)
    val buildings = new Buildings(Map(id → b))
    val config = GameConfigMock.gameConfig(shooting = GameConfigMock.shootingMock(shootRadius = 3, speed = 0.5, shootInterval = 10))

    val unit1 = GameUnitTest.unit(
      startPos = new Point(1, 1),
      endPos = new Point(5, 5),
      startTime = 1990
    )
    val unit2 = GameUnitTest.unit(
      startPos = new Point(0, 0),
      endPos = new Point(0, 0),
      startTime = 1990
    )
    val unit3 = GameUnitTest.unit(
      startPos = new Point(10, 20),
      endPos = new Point(0, 0),
      startTime = 1990
    )
    val units = new GameUnits(List(unit1, unit2, unit3))
    val optionBullets = Bullets.createOptionBullets(buildings, units, 1990, config, PlayerStateTest.playerStates).toList
    optionBullets.size should be(1)
    optionBullets(0).isDefined should be(true)
    optionBullets(0).get.unit should be(unit1)
  }

  "createBullet" should "be correct" in {
    val b = BuildingTest.building()
    val u = GameUnitTest.unit()
    val config = GameConfigMock.gameConfig(shooting = GameConfigMock.shootingMock(speed = 0.005))
    val bullet = Bullets.createBullet(b, u, 1990, config)
    bullet.building should be(b)
    bullet.unit should be(u)
    val duration: Double = b.pos.duration(u.getPos(1990), config.bulletSpeed)
    bullet.duration should be(Math.floor(duration))
  }

  "bullets->addMessages" should "work with empty list" in {
    Bullets.`bullets→addMessage`(List.empty, 1990).size should be(0)
  }

  "bullets->addMessages" should "return messages" in {
    val bullet1 = BulletTest.bullet(building = BuildingTest.building(id = new BuildingId(0)))
    val bullet2 = BulletTest.bullet(building = BuildingTest.building(id = new BuildingId(1)))
    val bullet3 = BulletTest.bullet(building = BuildingTest.building(id = new BuildingId(2)))

    val addMessages = Bullets.`bullets→addMessage`(List(bullet1, bullet2, bullet3), 60).toList

    addMessages.size should be(3)

    addMessages(0).bulletDTO.getBuildingId.getId should be(0)

    addMessages(1).bulletDTO.getBuildingId.getId should be(1)

    addMessages(2).bulletDTO.getBuildingId.getId should be(2)
  }
}

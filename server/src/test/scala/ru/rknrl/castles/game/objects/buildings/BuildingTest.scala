package ru.rknrl.castles.game.objects.buildings

import org.scalatest._
import ru.rknrl.castles.account.objects.BuildingPrototype
import ru.rknrl.castles.game.objects.players.PlayerId
import ru.rknrl.dto.CommonDTO.{BuildingLevel, BuildingType}
import ru.rknrl.utils.Point

object BuildingTest {
  private val playerId0 = new PlayerId(0)
  private val playerId1 = new PlayerId(1)

  private val owner0 = Some(playerId0)
  private val owner1 = Some(playerId1)

  private val id0 = new BuildingId(0)

  def building(id: BuildingId = id0,
               prototype: BuildingPrototype = new BuildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_2),
               x: Double = 5,
               y: Double = 6,
               population: Double = 123,
               owner: Option[PlayerId] = None,
               strengthened: Boolean = false,
               strengtheningStartTime: Long = 13,
               lastShootTime: Long = 12) =
    new Building(
      id = id,
      prototype = prototype,
      new Point(x, y),
      population = population,
      owner = owner,
      strengthened = strengthened,
      strengtheningStartTime = strengtheningStartTime,
      lastShootTime = lastShootTime
    )
}

class BuildingTest extends FlatSpec with Matchers {

  import ru.rknrl.castles.game.objects.buildings.BuildingTest._

  it should "throw AssertionError when population < 0" in {
    a[AssertionError] should be thrownBy {
      building(population = -0.1)
    }
  }

  "floorPopulation" should "truncate double population" in {
    building(population = 1.0).floorPopulation should be(1)
    building(population = 999.0).floorPopulation should be(999)
    building(population = 10.3).floorPopulation should be(10)
    building(population = 10.5).floorPopulation should be(10)
    building(population = 10.8).floorPopulation should be(10)
  }

  "setPopulation" should "update double value" in {
    building(population = 1.0).setPopulation(4.0).population should be(4.0)
    building(population = 11.1).setPopulation(13.1345).population should be(13.1345)
  }

  "setPopulation" should "not change other values" in {
    val a = building()
    val b = a.setPopulation(4.0)

    b.id should be(a.id)
    b.prototype should be(a.prototype)
    b.pos.x should be(a.pos.x)
    b.pos.y should be(a.pos.y)
    b.owner should be(a.owner)
    b.strengthened should be(a.strengthened)
    b.strengtheningStartTime should be(a.strengtheningStartTime)
    b.lastShootTime should be(a.lastShootTime)
  }

  "setOwner" should "change owner" in {
    building(owner = None).setOwner(owner0).owner should be(owner0)
    building(owner = owner0).setOwner(None).owner should be(None)
    building(owner = owner0).setOwner(owner1).owner should be(owner1)
  }

  "setOwner" should "not change other values" in {
    val a = building()
    val b = a.setOwner(owner1)

    b.id should be(a.id)
    b.prototype should be(a.prototype)
    b.pos.x should be(a.pos.x)
    b.pos.y should be(a.pos.y)
    b.population should be(a.population)
    b.strengthened should be(a.strengthened)
    b.strengtheningStartTime should be(a.strengtheningStartTime)
    b.lastShootTime should be(a.lastShootTime)
  }

  "strength" should "set strengthened to true" in {
    building(strengthened = true).strength(123).strengthened should be(true)
    building(strengthened = false).strength(123).strengthened should be(true)
  }

  "strength" should "set strengtheningStartTime" in {
    building(strengthened = true).strength(123).strengtheningStartTime should be(123)
    building(strengthened = false).strength(422).strengtheningStartTime should be(422)
  }

  "strength" should "not change other values" in {
    val a = building()
    val b = a.strength(110)

    b.id should be(a.id)
    b.prototype should be(a.prototype)
    b.pos.x should be(a.pos.x)
    b.pos.y should be(a.pos.y)
    b.population should be(a.population)
    b.owner should be(a.owner)
    b.lastShootTime should be(a.lastShootTime)
  }

  "unstrength" should "set strengthened to false" in {
    building(strengthened = true).unstrength().strengthened should be(false)
    building(strengthened = false).unstrength().strengthened should be(false)
  }

  "unstrength" should "not change other values" in {
    val a = building()
    val b = a.unstrength()

    b.id should be(a.id)
    b.prototype should be(a.prototype)
    b.pos.x should be(a.pos.x)
    b.pos.y should be(a.pos.y)
    b.population should be(a.population)
    b.owner should be(a.owner)
    b.lastShootTime should be(a.lastShootTime)
  }

  "shoot" should "set lastShootTime" in {
    building(lastShootTime = 0).shoot(123).lastShootTime should be(123)
    building(lastShootTime = 10).shoot(20).lastShootTime should be(20)
  }

  "shoot" should "not change other values" in {
    val a = building()
    val b = a.shoot(110)

    b.id should be(a.id)
    b.prototype should be(a.prototype)
    b.pos.x should be(a.pos.x)
    b.pos.y should be(a.pos.y)
    b.population should be(a.population)
    b.owner should be(a.owner)
    b.strengthened should be(a.strengthened)
    b.strengtheningStartTime should be(a.strengtheningStartTime)
  }

  "differentWith" should "be false if floorPopulation are equals" in {
    building(population = 1) differentWith building(population = 1) should be(false)
    building(population = 1) differentWith building(population = 1.8) should be(false)
  }

  "differentWith" should "be true if floorPopulation are not equals" in {
    building(population = 1) differentWith building(population = 2) should be(true)
    building(population = 1.3) differentWith building(population = 2.2) should be(true)
  }

  "differentWith" should "be false if owner are equals" in {
    building(owner = None) differentWith building(owner = None) should be(false)
    building(owner = owner0) differentWith building(owner = owner0) should be(false)
  }

  "differentWith" should "be true if owners are not equals" in {
    building(owner = None) differentWith building(owner = owner0) should be(true)
    building(owner = owner0) differentWith building(owner = owner1) should be(true)
  }

  "differentWith" should "be false if strengthened are equals" in {
    building(strengthened = false) differentWith building(strengthened = false) should be(false)
    building(strengthened = true) differentWith building(strengthened = true) should be(false)
  }

  "differentWith" should "be true if strengthened are not equals" in {
    building(strengthened = false) differentWith building(strengthened = true) should be(true)
    building(strengthened = true) differentWith building(strengthened = false) should be(true)
  }

  "differentWith" should "be false if all parameters are equals" in {
    building(population = 1.1, owner = owner0, strengthened = true) differentWith building(population = 1.2, owner = owner0, strengthened = true) should be(false)
  }

  "differentWith" should "be true if one parameter are not equals" in {
    building(population = 1.1, owner = owner0, strengthened = true) differentWith building(population = 2, owner = owner0, strengthened = true) should be(true)
    building(population = 1.1, owner = owner0, strengthened = true) differentWith building(population = 1.1, owner = None, strengthened = true) should be(true)
    building(population = 1.1, owner = owner0, strengthened = true) differentWith building(population = 1.1, owner = owner0, strengthened = false) should be(true)
  }

  "dto" should "set immutable values correctly" in {
    val dto = building(
      id = new BuildingId(7),
      prototype = new BuildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_2),
      x = 9.12,
      y = 8.24).dto

    dto.getId.getId should be(7)
    dto.getBuilding.getType should be(BuildingType.TOWER)
    dto.getBuilding.getLevel should be(BuildingLevel.LEVEL_2)
    dto.getPos.getX should be(9.12.toFloat)
    dto.getPos.getY should be(8.24.toFloat)
  }

  "dto" should "set population correctly" in {
    building(population = 234).dto.getPopulation should be(234)
    building(population = 234.7).dto.getPopulation should be(234)
  }

  "dto" should "set strengthened correctly" in {
    building(strengthened = true).dto.getStrengthened should be(true)
    building(strengthened = false).dto.getStrengthened should be(false)
  }

  "dto" should "set owner correctly" in {
    building(owner = None).dto.hasOwner should be(false)
    val dto = building(owner = owner0).dto
    dto.hasOwner should be(true)
    dto.getOwner.getId should be(owner0.get.id)
  }

  "updateDto" should "set id correctly" in {
    val dto = building(id = new BuildingId(7)).updateDto
    dto.getId.getId should be(7)
  }

  "updateDto" should "set population correctly" in {
    building(population = 234).updateDto.getPopulation should be(234)
    building(population = 234.7).updateDto.getPopulation should be(234)
  }

  "updateDto" should "set strengthened correctly" in {
    building(strengthened = true).updateDto.getStrengthened should be(true)
    building(strengthened = false).updateDto.getStrengthened should be(false)
  }

  "updateDto" should "set owner correctly" in {
    building(owner = None).updateDto.hasOwner should be(false)
    val dto = building(owner = owner0).updateDto
    dto.hasOwner should be(true)
    dto.getOwner.getId should be(owner0.get.id)
  }
}
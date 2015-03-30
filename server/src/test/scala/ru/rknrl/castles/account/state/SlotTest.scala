//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account.state

import org.scalatest.{FunSuite, Matchers}
import ru.rknrl.dto.AccountDTO.SlotDTO
import ru.rknrl.dto.CommonDTO.BuildingLevel._
import ru.rknrl.dto.CommonDTO.BuildingPrototypeDTO
import ru.rknrl.dto.CommonDTO.BuildingType._
import ru.rknrl.dto.CommonDTO.SlotId._

class SlotTest extends FunSuite with Matchers {

  def notEmptySlot = Slot(SLOT_1, BuildingPrototype(CHURCH, LEVEL_2))

  def emptySlot = Slot.empty(SLOT_3)

  test("parse from dto without building") {
    val dto = SlotDTO.newBuilder()
      .setId(SLOT_3)
      .build

    Slot(dto).id shouldBe SLOT_3
    Slot(dto).buildingPrototype shouldBe empty
  }

  test("parse dto with building") {
    val dto = SlotDTO.newBuilder()
      .setId(SLOT_1)
      .setBuildingPrototype(
        BuildingPrototypeDTO.newBuilder()
          .setType(CHURCH)
          .setLevel(LEVEL_2)
          .build
      )
      .build

    Slot(dto).id shouldBe SLOT_1
    Slot(dto).buildingPrototype.get shouldBe BuildingPrototype(CHURCH, LEVEL_2)
  }

  test("dto without building") {
    val dto = emptySlot.dto
    dto.getId shouldBe SLOT_3
    dto.hasBuildingPrototype shouldBe false
  }

  test("dto with building") {
    val dto = notEmptySlot.dto
    dto.getId shouldBe SLOT_1
    dto.hasBuildingPrototype shouldBe true
    dto.getBuildingPrototype.getType shouldBe CHURCH
    dto.getBuildingPrototype.getLevel shouldBe LEVEL_2
  }

  test("set on not empty slot") {
    val newSlot = notEmptySlot.set(BuildingPrototype(TOWER, LEVEL_2))
    newSlot.id shouldBe SLOT_1
    newSlot.buildingPrototype.get shouldBe BuildingPrototype(TOWER, LEVEL_2)
  }

  test("set on empty slot") {
    val newSlot = emptySlot.set(BuildingPrototype(TOWER, LEVEL_2))
    newSlot.id shouldBe SLOT_3
    newSlot.buildingPrototype.get shouldBe BuildingPrototype(TOWER, LEVEL_2)
  }

  test("remove on not empty slot") {
    val newSlot = notEmptySlot.remove
    newSlot.id shouldBe SLOT_1
    newSlot.buildingPrototype shouldBe empty
  }

  test("remove on empty slot") {
    a[Exception] shouldBe thrownBy {
      emptySlot.remove
    }
  }

  test("build on empty slot") {
    val newSlot = emptySlot.build(TOWER)
    newSlot.id shouldBe SLOT_3
    newSlot.buildingPrototype shouldBe Some(BuildingPrototype(TOWER, LEVEL_1))
  }

  test("build on not empty slot") {
    a[Exception] shouldBe thrownBy {
      notEmptySlot.build(TOWER)
    }
  }

  test("upgrade on not empty slot") {
    val newSlot = notEmptySlot.upgrade
    newSlot.id shouldBe SLOT_1
    newSlot.buildingPrototype shouldBe Some(BuildingPrototype(CHURCH, LEVEL_3))
  }

  test("upgrade on empty slot") {
    a[Exception] shouldBe thrownBy {
      emptySlot.upgrade
    }
  }

  test("upgrade on last level slot") {
    a[Exception] shouldBe thrownBy {
      Slot(SLOT_1, BuildingPrototype(CHURCH, LEVEL_3)).upgrade
    }
  }

}

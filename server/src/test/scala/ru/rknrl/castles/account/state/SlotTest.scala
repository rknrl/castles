//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account.state

import org.scalatest.{FreeSpec, Matchers}
import ru.rknrl.dto.AccountDTO.SlotDTO
import ru.rknrl.dto.CommonDTO.BuildingLevel._
import ru.rknrl.dto.CommonDTO.BuildingPrototypeDTO
import ru.rknrl.dto.CommonDTO.BuildingType._
import ru.rknrl.dto.CommonDTO.SlotId._

class SlotTest extends FreeSpec with Matchers {

  def notEmptySlot = Slot(SLOT_1, BuildingPrototype(CHURCH, LEVEL_2))

  def emptySlot = Slot.empty(SLOT_3)

  "set" - {
    "set on not empty slot" in {
      val newSlot = notEmptySlot.set(BuildingPrototype(TOWER, LEVEL_2))
      newSlot.id shouldBe SLOT_1
      newSlot.buildingPrototype.get shouldBe BuildingPrototype(TOWER, LEVEL_2)
    }

    "set on empty slot" in {
      val newSlot = emptySlot.set(BuildingPrototype(TOWER, LEVEL_2))
      newSlot.id shouldBe SLOT_3
      newSlot.buildingPrototype.get shouldBe BuildingPrototype(TOWER, LEVEL_2)
    }
  }

  "remove" - {
    "remove on not empty slot" in {
      val newSlot = notEmptySlot.remove
      newSlot.id shouldBe SLOT_1
      newSlot.buildingPrototype shouldBe empty
    }

    "remove on empty slot" in {
      a[Exception] shouldBe thrownBy {
        emptySlot.remove
      }
    }
  }

  "build" - {
    "build on empty slot" in {
      val newSlot = emptySlot.build(TOWER)
      newSlot.id shouldBe SLOT_3
      newSlot.buildingPrototype shouldBe Some(BuildingPrototype(TOWER, LEVEL_1))
    }

    "build on not empty slot" in {
      a[Exception] shouldBe thrownBy {
        notEmptySlot.build(TOWER)
      }
    }
  }

  "upgrade" - {
    "upgrade on not empty slot" in {
      val newSlot = notEmptySlot.upgrade
      newSlot.id shouldBe SLOT_1
      newSlot.buildingPrototype shouldBe Some(BuildingPrototype(CHURCH, LEVEL_3))
    }

    "upgrade on empty slot" in {
      a[Exception] shouldBe thrownBy {
        emptySlot.upgrade
      }
    }

    "upgrade on last level slot" in {
      a[Exception] shouldBe thrownBy {
        Slot(SLOT_1, BuildingPrototype(CHURCH, LEVEL_3)).upgrade
      }
    }
  }

  "dto" - {
    "dto without building" in {
      val dto = emptySlot.dto
      dto.getId shouldBe SLOT_3
      dto.hasBuildingPrototype shouldBe false
    }

    "dto with building" in {
      val dto = notEmptySlot.dto
      dto.getId shouldBe SLOT_1
      dto.hasBuildingPrototype shouldBe true
      dto.getBuildingPrototype.getType shouldBe CHURCH
      dto.getBuildingPrototype.getLevel shouldBe LEVEL_2
    }
  }

  "parse from dto" - {
    "parse from dto without building" in {
      val dto = SlotDTO.newBuilder()
        .setId(SLOT_3)
        .build

      Slot(dto).id shouldBe SLOT_3
      Slot(dto).buildingPrototype shouldBe empty
    }

    "parse dto with building" in {
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
  }
}

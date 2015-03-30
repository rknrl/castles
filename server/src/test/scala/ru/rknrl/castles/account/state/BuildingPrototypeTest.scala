//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account.state

import org.scalatest.{FreeSpec, Matchers}
import ru.rknrl.TestUtils._
import ru.rknrl.dto.CommonDTO.BuildingLevel._
import ru.rknrl.dto.CommonDTO.BuildingType._
import ru.rknrl.dto.CommonDTO.{BuildingLevel, BuildingPrototypeDTO, BuildingType}

class BuildingPrototypeTest extends FreeSpec with Matchers {
  "upgraded" in {
    BuildingPrototype(HOUSE, LEVEL_1).upgraded shouldBe BuildingPrototype(HOUSE, LEVEL_2)
    BuildingPrototype(TOWER, LEVEL_2).upgraded shouldBe BuildingPrototype(TOWER, LEVEL_3)
    a[Exception] shouldBe thrownBy {
      BuildingPrototype(CHURCH, LEVEL_3).upgraded
    }
  }

  "dto" in {
    val dto = BuildingPrototype(HOUSE, LEVEL_1).dto
    dto.getType shouldEqual HOUSE
    dto.getLevel shouldEqual LEVEL_1

    val dto2 = BuildingPrototype(TOWER, LEVEL_3).dto
    dto2.getType shouldEqual TOWER
    dto2.getLevel shouldEqual LEVEL_3
  }

  "parse from dto" in {
    val dto = BuildingPrototypeDTO.newBuilder()
      .setType(HOUSE)
      .setLevel(LEVEL_1)
      .build

    BuildingPrototype(dto) shouldBe BuildingPrototype(HOUSE, LEVEL_1)

    val dto2 = BuildingPrototypeDTO.newBuilder()
      .setType(TOWER)
      .setLevel(LEVEL_3)
      .build

    BuildingPrototype(dto2) shouldBe BuildingPrototype(TOWER, LEVEL_3)
  }

  val all = for (buildingType ← BuildingType.values;
                 buildingLevel ← BuildingLevel.values)
    yield () ⇒ BuildingPrototype(buildingType, buildingLevel)

  "equals" in {
    checkEquals(all)
  }

  "hashCode" in {
    checkHashCode(all)
  }
}

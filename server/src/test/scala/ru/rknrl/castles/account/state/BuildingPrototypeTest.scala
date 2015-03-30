//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account.state

import org.scalatest.{FunSuite, Matchers}
import ru.rknrl.TestUtils._
import ru.rknrl.dto.CommonDTO.BuildingLevel._
import ru.rknrl.dto.CommonDTO.BuildingPrototypeDTO
import ru.rknrl.dto.CommonDTO.BuildingType._

class BuildingPrototypeTest extends FunSuite with Matchers {
  test("dto") {
    val dto = BuildingPrototype(HOUSE, LEVEL_1).dto
    dto.getType shouldEqual HOUSE
    dto.getLevel shouldEqual LEVEL_1

    val dto2 = BuildingPrototype(TOWER, LEVEL_3).dto
    dto2.getType shouldEqual TOWER
    dto2.getLevel shouldEqual LEVEL_3
  }

  test("parse from dto") {
    val dto = BuildingPrototypeDTO.newBuilder()
      .setType(HOUSE)
      .setLevel(LEVEL_1)
      .build

    BuildingPrototype(dto).buildingType shouldBe HOUSE
    BuildingPrototype(dto).level shouldBe LEVEL_1

    val dto2 = BuildingPrototypeDTO.newBuilder()
      .setType(TOWER)
      .setLevel(LEVEL_3)
      .build

    BuildingPrototype(dto2).buildingType shouldBe TOWER
    BuildingPrototype(dto2).level shouldBe LEVEL_3
  }

  test("upgraded") {
    BuildingPrototype(HOUSE, LEVEL_1).upgraded shouldBe BuildingPrototype(HOUSE, LEVEL_2)
    BuildingPrototype(TOWER, LEVEL_2).upgraded shouldBe BuildingPrototype(TOWER, LEVEL_3)
    a[Exception] shouldBe thrownBy {
      BuildingPrototype(CHURCH, LEVEL_3).upgraded
    }
  }

  test("equals") {
    checkEquals(
      () ⇒ BuildingPrototype(HOUSE, LEVEL_1),
      () ⇒ BuildingPrototype(CHURCH, LEVEL_2)
    )
  }

  test("hashCode") {
    checkHashCode(
      () ⇒ BuildingPrototype(HOUSE, LEVEL_1),
      () ⇒ BuildingPrototype(CHURCH, LEVEL_2)
    )
  }
}

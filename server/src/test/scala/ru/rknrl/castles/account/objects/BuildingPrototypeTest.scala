package ru.rknrl.castles.account.objects

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.dto.CommonDTO.{BuildingLevel, BuildingType}

class BuildingPrototypeTest extends FlatSpec with Matchers {
  "BuildingPrototype.dto" should "be correct" in {
    val dto = new BuildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_3).dto
    dto.getType should be(BuildingType.TOWER)
    dto.getLevel should be(BuildingLevel.LEVEL_3)
  }

  "BuildingPrototype.equals" should "be false with other types" in {
    (new BuildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_3) == 124) should be(false)
  }

  "BuildingPrototype.equals" should "be true with same playerId" in {
    (new BuildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_3) == new BuildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_3)) should be(true)
  }

  "BuildingPrototype.equals" should "be true with different playerId" in {
    (new BuildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_3) == new BuildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_2)) should be(false)
    (new BuildingPrototype(BuildingType.HOUSE, BuildingLevel.LEVEL_3) == new BuildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_3)) should be(false)
  }

  "BuildingPrototype" should "have correct hash" in {
    Map[BuildingPrototype, String]()
      .updated(new BuildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_3), "a")
      .updated(new BuildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_3), "b")
      .apply(new BuildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_3)) should be("b")

    Map[BuildingPrototype, String]()
      .updated(new BuildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_3), "a")
      .updated(new BuildingPrototype(BuildingType.HOUSE, BuildingLevel.LEVEL_3), "b")
      .apply(new BuildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_3)) should be("a")
  }

  "getNextLevel" should "return prototype" in {
    BuildingPrototype.getNextLevel(BuildingLevel.LEVEL_1) should be(BuildingLevel.LEVEL_2)
    BuildingPrototype.getNextLevel(BuildingLevel.LEVEL_2) should be(BuildingLevel.LEVEL_3)
  }

  "getNextLevel" should "throw Exception on last level" in {
    a[Exception] should be thrownBy {
      BuildingPrototype.getNextLevel(BuildingLevel.LEVEL_3)
    }
  }
}

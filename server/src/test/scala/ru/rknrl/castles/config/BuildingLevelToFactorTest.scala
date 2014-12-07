package ru.rknrl.castles.config

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.account.AccountConfig.BuildingPrices
import ru.rknrl.castles.config.BuildingLevelToFactorTest.buildingLevelToFactor
import ru.rknrl.castles.config.Config.BuildingLevelToFactor
import ru.rknrl.castles.config.ConfigJsonProtocol._
import ru.rknrl.dto.CommonDTO.BuildingLevel
import spray.json._

object BuildingLevelToFactorTest {
  val buildingLevelToFactor = """{"level1":1.7,"level2":1.2,"level3":1.1}"""
}

class BuildingLevelToFactorTest extends FlatSpec with Matchers {

  "BuildingLevelToFactor" should "be correct deserialize from json" in {
    val prices = buildingLevelToFactor.parseJson.convertTo[BuildingLevelToFactor]
    prices(BuildingLevel.LEVEL_1) should be(1.7)
    prices(BuildingLevel.LEVEL_2) should be(1.2)
    prices(BuildingLevel.LEVEL_3) should be(1.1)
  }

  "BuildingLevelToFactor without all levels" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """{"level1":10,"level3":30}""".parseJson.convertTo[BuildingPrices]
    }
  }

}
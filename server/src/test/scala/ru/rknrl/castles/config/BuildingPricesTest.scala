package ru.rknrl.castles.config

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.account.AccountConfig.BuildingPrices
import ru.rknrl.castles.config.BuildingPricesTest.buildingPrices
import ru.rknrl.castles.config.ConfigJsonProtocol._
import ru.rknrl.dto.CommonDTO.BuildingLevel
import spray.json._

object BuildingPricesTest {
  val buildingPrices = """{"level1":10,"level2":20,"level3":30}"""
}

class BuildingPricesTest extends FlatSpec with Matchers {
  "BuildingPrices" should "be correct deserialize from json" in {
    val prices = buildingPrices.parseJson.convertTo[BuildingPrices]
    prices(BuildingLevel.LEVEL_1) should be(10)
    prices(BuildingLevel.LEVEL_2) should be(20)
    prices(BuildingLevel.LEVEL_3) should be(30)
  }

  "BuildingPrices without all levels" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """{"level1":10,"level3":30}""".parseJson.convertTo[BuildingPrices]
    }
  }

  "BuildingPrices with number price" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """{"level1":10,"level2":20.2,"level3":30}""".parseJson.convertTo[BuildingPrices]
    }
  }

}
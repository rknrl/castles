package ru.rknrl.castles.config

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.account.AccountConfig
import ru.rknrl.castles.config.AccountConfigTest.account
import ru.rknrl.castles.config.ConfigJsonProtocol._
import ru.rknrl.dto.CommonDTO.BuildingLevel
import spray.json._

object AccountConfigTest {
  val account =
    s"""
      |{
      |"itemPrice":100,
      |"goldByDollar":777,
      |"buildingPrices":${BuildingPricesTest.buildingPrices}
      |}
    """.stripMargin
}

class AccountConfigTest extends FlatSpec with Matchers {
  "AccountConfig" should "be correct deserialize from json" in {
    val a = account.parseJson.convertTo[AccountConfig]

    a.buildingPrices(BuildingLevel.LEVEL_1) should be(10)
    a.itemPrice should be(100)
    a.goldByDollar should be(777)
  }

  "AccountConfig without building prices" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """
        |{
        |"itemPrice":100,
        |"goldByDollar":777
        |}
      """.stripMargin.parseJson.convertTo[AccountConfig]
    }
  }

  "AccountConfig without itemPrice" should "be throw exception" in {
    a[Exception] should be thrownBy {
      s"""
        |{
        |"goldByDollar":777,
        |"buildingPrices":${BuildingPricesTest.buildingPrices}
        |}
      """.stripMargin.parseJson.convertTo[AccountConfig]
    }
  }

  "AccountConfig with float itemPrice" should "be throw exception" in {
    a[Exception] should be thrownBy {
      s"""
        |{
        |"itemPrice":100,
        |"goldByDollar":777.77,
        |"buildingPrices":${BuildingPricesTest.buildingPrices}
        |}
      """.stripMargin.parseJson.convertTo[AccountConfig]
    }
  }

  "AccountConfig with float goldByDollar" should "be throw exception" in {
    a[Exception] should be thrownBy {
      s"""
        |{
        |"itemPrice":100.1212,
        |"goldByDollar":777,
        |"buildingPrices":${BuildingPricesTest.buildingPrices}
        |}
      """.stripMargin.parseJson.convertTo[AccountConfig]
    }
  }

}
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
      |"initGold":201,
      |"initItemCount":2,
      |"itemPrice":100,
      |"skillUpgradePrices":${SkillUpgradePricesTest.skillUpgradePrices},
      |"buildingPrices":${BuildingPricesTest.buildingPrices}
      |}
    """.stripMargin
}

class AccountConfigTest extends FlatSpec with Matchers {
  "AccountConfig" should "be correct deserialize from json" in {
    val a = account.parseJson.convertTo[AccountConfig]

    a.initGold should be(201)
    a.initItemCount should be(2)
    a.buildingPrices(BuildingLevel.LEVEL_1) should be(10)
    a.itemPrice should be(100)
  }

  "AccountConfig without initGold" should "be throw exception" in {
    a[Exception] should be thrownBy {
      s"""
        |{
        |"initItemCount":2,
        |"itemPrice":100,
        |"goldByDollar":777,
        |"buildingPrices":${BuildingPricesTest.buildingPrices},
        |"skillUpgradePrices":${SkillUpgradePricesTest.skillUpgradePrices}
        |}
      """.stripMargin.parseJson.convertTo[AccountConfig]
    }
  }

  "AccountConfig with float initGold" should "be throw exception" in {
    a[Exception] should be thrownBy {
      s"""
        |{
        |"initGold":101.1,
        |"initItemCount":2,
        |"itemPrice":100,
        |"goldByDollar":777,
        |"buildingPrices":${BuildingPricesTest.buildingPrices},
        |"skillUpgradePrices":${SkillUpgradePricesTest.skillUpgradePrices}
        |}
      """.stripMargin.parseJson.convertTo[AccountConfig]
    }
  }

  "AccountConfig with float initItemCount" should "be throw exception" in {
    a[Exception] should be thrownBy {
      s"""
        |{
        |"initGold":101,
        |"initItemCount":2.2,
        |"itemPrice":100,
        |"goldByDollar":777,
        |"buildingPrices":${BuildingPricesTest.buildingPrices},
        |"skillUpgradePrices":${SkillUpgradePricesTest.skillUpgradePrices}
        |}
      """.stripMargin.parseJson.convertTo[AccountConfig]
    }
  }


  "AccountConfig without skillUpgradePrices" should "be throw exception" in {
    a[Exception] should be thrownBy {
      s"""
        |{
        |"initGold":101,
        |"initItemCount":2,
        |"itemPrice":100,
        |"goldByDollar":777,
        |"buildingPrices":${BuildingPricesTest.buildingPrices}
        |}
      """.stripMargin.parseJson.convertTo[AccountConfig]
    }
  }

  "AccountConfig without building prices" should "be throw exception" in {
    a[Exception] should be thrownBy {
      s"""
        |{
        |"initGold":101,
        |"initItemCount":2,
        |"itemPrice":100,
        |"goldByDollar":777,
        |"skillUpgradePrices":${SkillUpgradePricesTest.skillUpgradePrices}
        |}
      """.stripMargin.parseJson.convertTo[AccountConfig]
    }
  }

  "AccountConfig without itemPrice" should "be throw exception" in {
    a[Exception] should be thrownBy {
      s"""
        |{
        |"initGold":101,
        |"initItemCount":2,
        |"goldByDollar":777,
        |"buildingPrices":${BuildingPricesTest.buildingPrices},
        |"skillUpgradePrices":${SkillUpgradePricesTest.skillUpgradePrices},
        |}
      """.stripMargin.parseJson.convertTo[AccountConfig]
    }
  }

  "AccountConfig with float itemPrice" should "be throw exception" in {
    a[Exception] should be thrownBy {
      s"""
        |{
        |"initGold":101,
        |"initItemCount":2,
        |"itemPrice":100,
        |"goldByDollar":777.77,
        |"buildingPrices":${BuildingPricesTest.buildingPrices},
        |"skillUpgradePrices":${SkillUpgradePricesTest.skillUpgradePrices},
        |}
      """.stripMargin.parseJson.convertTo[AccountConfig]
    }
  }

  "AccountConfig with float goldByDollar" should "be throw exception" in {
    a[Exception] should be thrownBy {
      s"""
        |{
        |"initGold":101,
        |"initItemCount":2,
        |"itemPrice":100.1212,
        |"goldByDollar":777,
        |"buildingPrices":${BuildingPricesTest.buildingPrices},
        |"skillUpgradePrices":${SkillUpgradePricesTest.skillUpgradePrices},
        |}
      """.stripMargin.parseJson.convertTo[AccountConfig]
    }
  }

}
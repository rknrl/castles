package ru.rknrl.castles.config

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.account.AccountConfig.SkillUpgradePrices
import ru.rknrl.castles.config.ConfigJsonProtocol._
import ru.rknrl.castles.config.SkillUpgradePricesTest.skillUpgradePrices
import spray.json._

object SkillUpgradePricesTest {
  val skillUpgradePrices =
    """
      |{
      |"1":1,
      |"2":2,
      |"3":4,
      |"4":8,
      |"5":16,
      |"6":32,
      |"7":64,
      |"8":128,
      |"9":256
      |}
    """.stripMargin
}

class SkillUpgradePricesTest extends FlatSpec with Matchers {
  "SkillUpgradePrices" should "be correct deserialize from json" in {
    val prices = skillUpgradePrices.parseJson.convertTo[SkillUpgradePrices]
    prices(1) should be(1)
    prices(2) should be(2)
    prices(3) should be(4)
    prices(4) should be(8)
    prices(5) should be(16)
    prices(6) should be(32)
    prices(7) should be(64)
    prices(8) should be(128)
    prices(9) should be(256)
  }

  "SkillUpgradePrices without all levels" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """{"1":10,"2":30}""".parseJson.convertTo[SkillUpgradePrices]
    }
  }

  "SkillUpgradePrices with number price" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """
        |{
        |"1":1,
        |"2":2,
        |"3":4,
        |"4":8,
        |"5":16.4,
        |"6":32,
        |"7":64,
        |"8":128,
        |"9":256,
        |}
      """.stripMargin.parseJson.convertTo[SkillUpgradePrices]
    }
  }

}
package ru.rknrl.castles.config

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.config.ConfigJsonProtocol._
import ru.rknrl.castles.config.ConfigTest.config
import spray.json._

object ConfigTest {
  val socialConfigs =
    """
      |{
      |"vk":{
      |"appId":"vkAppId",
      |"appSecret":"vkAppSecret",
      |"productsInfo": {
      |"1": {"count": 100, "price": 123},
      |"2": {"count": 100, "price": 321}
      |}
      |},
      |"ok":{
      |"appId":"okAppId",
      |"appSecret":"okAppSecret",
      |"productsInfo": {
      |"1": {"count": 100, "price": 123},
      |"2": {"count": 100, "price": 321}
      |}
      |},
      |"mm":{
      |"appId":"mmAppId",
      |"appSecret":"mmAppSecret",
      |"productsInfo": {
      |"1": {"count": 100, "price": 123},
      |"2": {"count": 100, "price": 321}
      |}
      |}
      |}
    """.stripMargin

  val productsMock =
    """
      |[
      |    {
      |      "id": 1,
      |      "title": "Звезды",
      |      "description": "Ведь если звезды зажигают-значит-это кому-нибудь нужно?",
      |      "photoUrl": "http://castles.rknrl.ru/start.png"
      |    },
      |    {
      |      "id": 2,
      |      "title": "Платиновый аккаунт",
      |      "description": "Ведь если платиновый аккаунт зажигают-значит-это кому-нибудь нужно?",
      |      "photoUrl": "http://castles.rknrl.ru/platinum.png"
      |    }
      |]
    """.stripMargin

  val config =
    s"""
      |{
      |  "host": "127.0.0.1",
      |  "gamePort": 2335,
      |  "policyPort": 2336,
      |  "products": $productsMock,
      |  "account": ${AccountConfigTest.account},
      |  "game": ${GameConfigTest.game},
      |  "social": $socialConfigs
      |}
      """

  val configMock = config.stripMargin.parseJson.convertTo[Config]

}

class ConfigTest extends FlatSpec with Matchers {
  "Config" should "be correct deserialize from json" in {
    val c = config.stripMargin.parseJson.convertTo[Config]
    c.host should be("127.0.0.1")
    c.gamePort should be(2335)
    c.policyPort should be(2336)
  }

  "Config without host" should "throw Exception" in {
    a[Exception] should be thrownBy {
      s"""
      |{
      |  "gamePort": 2335,
      |  "policyPort": 2336,
      |  "products: ${ConfigTest.productsMock},"
      |  "account": ${AccountConfigTest.account},
      |  "game": ${GameConfigTest.game},
      |  "social": ${ConfigTest.socialConfigs}
      |}
      """.stripMargin.parseJson.convertTo[Config]
    }
  }

  "Config without gamePort" should "throw Exception" in {
    a[Exception] should be thrownBy {
      s"""
      |{
      |  "host": "127.0.0.1",
      |  "policyPort": 2336,
      |  "products: ${ConfigTest.productsMock},"
      |  "account": ${AccountConfigTest.account},
      |  "game": ${GameConfigTest.game},
      |  "social": ${ConfigTest.socialConfigs}
      |}
      """.stripMargin.parseJson.convertTo[Config]
    }
  }

  "Config without policyPort" should "throw Exception" in {
    a[Exception] should be thrownBy {
      s"""
      |{
      |  "host": "127.0.0.1",
      |  "gamePort": 2335,
      |  "products: ${ConfigTest.productsMock},"
      |  "account": ${AccountConfigTest.account},
      |  "game": ${GameConfigTest.game},
      |  "social": ${ConfigTest.socialConfigs}
      |}
      """.stripMargin.parseJson.convertTo[Config]
    }
  }

  "Config without products" should "throw Exception" in {
    a[Exception] should be thrownBy {
      s"""
      |{
      |  "host": "127.0.0.1",
      |  "gamePort": 2335,
      |  "account": ${AccountConfigTest.account},
      |  "game": ${GameConfigTest.game},
      |  "social": ${ConfigTest.socialConfigs}
      |}
      """.stripMargin.parseJson.convertTo[Config]
    }
  }

  "Config without account" should "throw Exception" in {
    a[Exception] should be thrownBy {
      s"""
      |{
      |  "host": "127.0.0.1",
      |  "gamePort": 2335,
      |  "products: ${ConfigTest.productsMock},"
      |  "game": ${GameConfigTest.game},
      |  "social": ${ConfigTest.socialConfigs}
      |}
      """.stripMargin.parseJson.convertTo[Config]
    }
  }

  "Config without game" should "throw Exception" in {
    a[Exception] should be thrownBy {
      s"""
      |{
      |  "host": "127.0.0.1",
      |  "gamePort": 2335,
      |  "products: ${ConfigTest.productsMock},"
      |  "account": ${AccountConfigTest.account},
      |  "social": ${ConfigTest.socialConfigs}
      |}
      """.stripMargin.parseJson.convertTo[Config]
    }
  }

  "Config without social" should "throw Exception" in {
    a[Exception] should be thrownBy {
      s"""
      |{
      |  "host": "127.0.0.1",
      |  "gamePort": 2335,
      |  "products: ${ConfigTest.productsMock},"
      |  "account": ${AccountConfigTest.account},
      |  "game": ${GameConfigTest.game}
      |}
      """.stripMargin.parseJson.convertTo[Config]
    }
  }

}

package ru.rknrl.castles.config

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.config.ConfigJsonProtocol._
import ru.rknrl.castles.config.ConfigTest.config
import spray.json._

object ConfigTest {
  val socialConfigs =
    """
      |{
      |"vk":{"appId":"vkAppId","appSecret":"vkAppSecret"},
      |"ok":{"appId":"okAppId","appSecret":"okAppSecret"},
      |"mm":{"appId":"mmAppId","appSecret":"mmAppSecret"}
      |}
    """.stripMargin

  val config =
    s"""
      |{
      |  "tcpIp": "127.0.0.1",
      |  "tcpPort": 2335,
      |  "account": ${AccountConfigTest.account},
      |  "game": ${GameConfigTest.game},
      |  "social": $socialConfigs
      |}
      """

}

class ConfigTest extends FlatSpec with Matchers {
  "Config" should "be correct deserialize from json" in {
    val c = config.stripMargin.parseJson.convertTo[Config]
    c.tcpIp should be("127.0.0.1")
    c.tcpPort should be(2335)
  }

  "Config without tcpId" should "throw Exception" in {
    a[Exception] should be thrownBy {
      s"""
      |{
      |  "tcpPort": 2335,
      |  "account": ${AccountConfigTest.account},
      |  "game": ${GameConfigTest.game},
      |  "social": ${ConfigTest.socialConfigs}
      |}
      """.stripMargin.parseJson.convertTo[Config]
    }
  }

  "Config without tcpPort" should "throw Exception" in {
    a[Exception] should be thrownBy {
      s"""
      |{
      |  "tcpIp": "127.0.0.1",
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
      |  "tcpIp": "127.0.0.1",
      |  "tcpPort": 2335,
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
      |  "tcpIp": "127.0.0.1",
      |  "tcpPort": 2335,
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
      |  "tcpIp": "127.0.0.1",
      |  "tcpPort": 2335,
      |  "account": ${AccountConfigTest.account},
      |  "game": ${GameConfigTest.game}
      |}
      """.stripMargin.parseJson.convertTo[Config]
    }
  }

}

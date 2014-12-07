package ru.rknrl.castles.config

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.config.AssistanceConfigTest.assistance
import ru.rknrl.castles.config.ConfigJsonProtocol._
import ru.rknrl.castles.game.AssistanceConfig
import spray.json._

object AssistanceConfigTest {
  val assistance = """{"count":5000}"""
}

class AssistanceConfigTest extends FlatSpec with Matchers {
  "AssistanceConfig" should "be correct deserialize from json" in {
    val s = assistance.parseJson.convertTo[AssistanceConfig]
    s.count should be(5000)
  }

  "AssistanceConfig without count" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """{}""".parseJson.convertTo[AssistanceConfig]
    }
  }

  "AssistanceConfig with float count" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """{"count":5000.1}""".parseJson.convertTo[AssistanceConfig]
    }
  }
}
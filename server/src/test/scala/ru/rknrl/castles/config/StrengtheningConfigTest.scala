package ru.rknrl.castles.config

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.config.ConfigJsonProtocol._
import ru.rknrl.castles.config.StrengtheningConfigTest.strengthening
import ru.rknrl.castles.game.StrengtheningConfig
import spray.json._

object StrengtheningConfigTest {
  val strengthening = """{"factor":11.11,"duration":5000}"""
}

class StrengtheningConfigTest extends FlatSpec with Matchers {
  "StrengtheningConfig" should "be correct deserialize from json" in {
    val s = strengthening.parseJson.convertTo[StrengtheningConfig]
    s.factor should be(11.11)
    s.duration should be(5000)
  }

  "StrengtheningConfig without factor" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """{"duration":5000}""".parseJson.convertTo[StrengtheningConfig]
    }
  }
  "StrengtheningConfig without duration" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """{"factor":5000}""".parseJson.convertTo[StrengtheningConfig]
    }
  }
  "StrengtheningConfig with float duration" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """{"factor":5000,"duration":11.11}""".parseJson.convertTo[StrengtheningConfig]
    }
  }
}
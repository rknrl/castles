package ru.rknrl.castles.config

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.config.ConfigJsonProtocol._
import ru.rknrl.castles.config.VolcanoConfigTest.volcano
import ru.rknrl.castles.game.VolcanoConfig
import spray.json._

object VolcanoConfigTest {
  val volcano = """{"damage":11.11,"duration":5000}"""
}

class VolcanoConfigTest extends FlatSpec with Matchers {

  "VolcanoConfig" should "be correct deserialize from json" in {
    val v = volcano.parseJson.convertTo[VolcanoConfig]
    v.damage should be(11.11)
    v.duration should be(5000)
  }

  "VolcanoConfig without damage" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """{"duration":5000}""".parseJson.convertTo[VolcanoConfig]
    }
  }
  "VolcanoConfig without duration" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """{"damage":5000}""".parseJson.convertTo[VolcanoConfig]
    }
  }
  "VolcanoConfig with float duration" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """{"damage":5000,"duration":11.11}""".parseJson.convertTo[VolcanoConfig]
    }
  }
}
package ru.rknrl.castles.config

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.config.ConfigJsonProtocol._
import ru.rknrl.castles.config.FireballConfigTest.fireball
import ru.rknrl.castles.game.FireballConfig
import spray.json._

object FireballConfigTest {
  val fireball = """{"damage":11.11,"flyDuration":1000}"""
}

class FireballConfigTest extends FlatSpec with Matchers {
  "FireballConfig" should "be correct deserialize from json" in {
    val f = fireball.parseJson.convertTo[FireballConfig]
    f.damage should be(11.11)
    f.flyDuration should be(1000)
  }

  "FireballConfig without damage" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """{"flyDuration":1000}""".parseJson.convertTo[FireballConfig]
    }
  }
  "FireballConfig without flyDuration" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """{"damage":11.11}""".parseJson.convertTo[FireballConfig]
    }
  }
  "FireballConfig with float flyDuration" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """{"flyDuration":11.11}""".parseJson.convertTo[FireballConfig]
    }
  }
}
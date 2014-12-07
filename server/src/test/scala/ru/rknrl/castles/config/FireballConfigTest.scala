package ru.rknrl.castles.config

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.config.ConfigJsonProtocol._
import ru.rknrl.castles.config.FireballConfigTest.fireball
import ru.rknrl.castles.game.FireballConfig
import spray.json._

object FireballConfigTest {
  val fireball = """{"damage":11.11}"""
}

class FireballConfigTest extends FlatSpec with Matchers {
  "FireballConfig" should "be correct deserialize from json" in {
    val f = fireball.parseJson.convertTo[FireballConfig]
    f.damage should be(11.11)
  }

  "FireballConfig without damage" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """{}""".parseJson.convertTo[FireballConfig]
    }
  }
}
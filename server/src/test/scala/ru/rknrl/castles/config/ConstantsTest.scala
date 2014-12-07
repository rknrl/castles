package ru.rknrl.castles.config

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.config.ConfigJsonProtocol._
import ru.rknrl.castles.config.ConstantsTest.constants
import ru.rknrl.castles.game.Constants
import spray.json._

object ConstantsTest {
  val constants = """{"unitToExitFactor":0.5,"itemCooldown":2222}"""
}

class ConstantsTest extends FlatSpec with Matchers {

  "Constants" should "be correct deserialize from json" in {
    val c = constants.parseJson.convertTo[Constants]
    c.unitToExitFactor should be(0.5)
    c.itemCooldown should be(2222)
  }

  "Constants without unitToExitFactor" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """{"itemCooldown":2222}""".parseJson.convertTo[Constants]
    }
  }

  "Constants without itemCooldown" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """{"unitToExitFactor":0.5}""".parseJson.convertTo[Constants]
    }
  }

  "Constants with float itemCooldown" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """{"unitToExitFactor":0.5,"itemCooldown":222.2}""".parseJson.convertTo[Constants]
    }
  }

}
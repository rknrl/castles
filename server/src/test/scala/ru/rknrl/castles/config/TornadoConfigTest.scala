package ru.rknrl.castles.config

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.config.ConfigJsonProtocol._
import ru.rknrl.castles.config.TornadoConfigTest.tornado
import ru.rknrl.castles.game.TornadoConfig
import spray.json._

object TornadoConfigTest {
  val tornado = """{"damage":11.11,"duration":5000,"speed":0.004}"""
}

class TornadoConfigTest extends FlatSpec with Matchers {
  "TornadoConfig" should "be correct deserialize from json" in {
    val t = tornado.parseJson.convertTo[TornadoConfig]
    t.damage should be(11.11)
    t.duration should be(5000)
    t.speed should be(0.004)
  }

  "TornadoConfig without damage" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """{"duration":5000,"speed":0.004}""".parseJson.convertTo[TornadoConfig]
    }
  }
  "TornadoConfig without duration" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """{"damage":11.11,"speed":0.004}""".parseJson.convertTo[TornadoConfig]
    }
  }
  "TornadoConfig without speed" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """{"damage":11.11,"duration":5000}""".parseJson.convertTo[TornadoConfig]
    }
  }
  "TornadoConfig with float duration" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """{"damage":5000,"duration":11.11,"speed":0.004}""".parseJson.convertTo[TornadoConfig]
    }
  }
}
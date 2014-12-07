package ru.rknrl.castles.config

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.config.ConfigJsonProtocol._
import ru.rknrl.castles.config.StatTest.stat
import ru.rknrl.castles.game.Stat
import spray.json._

object StatTest {
  val stat = """{"attack":11.11,"defence":22.222,"speed":33.33}"""
}

class StatTest extends FlatSpec with Matchers {
  "Stat" should "be correct deserialize from json" in {
    val s = stat.parseJson.convertTo[Stat]
    s.attack should be(11.11)
    s.defence should be(22.222)
    s.speed should be(33.33)
  }

  "Stat without attack" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """{"defence":22.222,"speed":33.333}""".parseJson.convertTo[Stat]
    }
  }

  "Stat without defence" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """{"attack":11.11,"speed":33.333}""".parseJson.convertTo[Stat]
    }
  }

  "Stat without speed" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """{"attack":11.11,"defence":22.222}""".parseJson.convertTo[Stat]
    }
  }

}
package ru.rknrl.castles.config

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.config.ConfigJsonProtocol._
import ru.rknrl.castles.config.ShootingConfigTest.shooting
import ru.rknrl.castles.game.ShootingConfig
import spray.json._

object ShootingConfigTest {
  val shooting =
    """
      |{
      |"damage":11.11,
      |"speed":0.004,
      |"shootInterval":4000,
      |"shootRadius":1.2
      |}
    """.stripMargin
}

class ShootingConfigTest extends FlatSpec with Matchers {
  "ShootingConfig" should "be correct deserialize from json" in {
    val s = shooting.parseJson.convertTo[ShootingConfig]
    s.damage should be(11.11)
    s.speed should be(0.004)
    s.shootInterval should be(4000)
    s.shootRadius should be(1.2)
  }

  "ShootingConfig without damage" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """
        |{
        |"speed":0.004,
        |"shootInterval":4000,
        |"shootRadius":1.2
        |}
      """.stripMargin.parseJson.convertTo[ShootingConfig]
    }
  }

  "ShootingConfig without speed" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """
        |{
        |"damage":11.11,
        |"shootInterval":4000,
        |"shootRadius":1.2
        |}
      """.stripMargin.parseJson.convertTo[ShootingConfig]
    }
  }

  "ShootingConfig without shootInterval" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """
        |{
        |"damage":11.11,
        |"speed":0.004,
        |"shootRadius":1.2
        |}
      """.stripMargin.parseJson.convertTo[ShootingConfig]
    }
  }

  "ShootingConfig without shootRadius" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """
        |{
        |"damage":11.11,
        |"speed":0.004,
        |"shootInterval":4000
        |}
      """.stripMargin.parseJson.convertTo[ShootingConfig]
    }
  }

  "ShootingConfig with float shootInterval" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """
        |{
        |"damage":11.11,
        |"speed":0.004,
        |"shootInterval":4000.4,
        |"shootRadius":1.2
        |}
      """.stripMargin.parseJson.convertTo[ShootingConfig]
    }
  }
}
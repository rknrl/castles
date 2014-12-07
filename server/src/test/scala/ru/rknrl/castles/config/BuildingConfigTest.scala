package ru.rknrl.castles.config

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.config.BuildingConfigTest.buildingConfig
import ru.rknrl.castles.config.ConfigJsonProtocol._
import ru.rknrl.castles.game.BuildingConfig
import spray.json._

object BuildingConfigTest {
  val buildingConfig =
    s"""
      |{
      |"regeneration":125.6,
      |"startPopulation":100,
      |"stat":${StatTest.stat}
      |}
    """.stripMargin
}

class BuildingConfigTest extends FlatSpec with Matchers {
  "BuildingConfig" should "be correct deserialize from json" in {
    val b = buildingConfig.parseJson.convertTo[BuildingConfig]
    b.regeneration should be(125.6)
    b.startPopulation should be(100)
    b.stat.attack should be(11.11)
  }

  "BuildingConfig without regeneration" should "be throw exception" in {
    a[Exception] should be thrownBy {
      s"""
        |{
        |"startPopulation":100,
        |"stat":${StatTest.stat}
        |}
      """.stripMargin.parseJson.convertTo[BuildingConfig]
    }
  }

  "BuildingConfig without startPopulation" should "be throw exception" in {
    a[Exception] should be thrownBy {
      s"""
        |{
        |"regeneration":125.6,
        |"stat":${StatTest.stat}
        |}
      """.stripMargin.parseJson.convertTo[BuildingConfig]
    }
  }

  "BuildingConfig without stat" should "be throw exception" in {
    a[Exception] should be thrownBy {
      """
        |{
        |"regeneration":125.6,
        |"startPopulation":100
        |}
      """.stripMargin.parseJson.convertTo[BuildingConfig]
    }
  }

  "BuildingConfig with float startPopulation" should "be throw exception" in {
    a[Exception] should be thrownBy {
      s"""
        |{
        |"startPopulation":100.8,
        |"regeneration":125.6,
        |"stat":${StatTest.stat}
        |}
      """.stripMargin.parseJson.convertTo[BuildingConfig]
    }
  }

}
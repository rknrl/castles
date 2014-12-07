package ru.rknrl.castles.config

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.config.BuildingsConfigTest.buildingsConfig
import ru.rknrl.castles.config.Config.BuildingsConfig
import ru.rknrl.castles.config.ConfigJsonProtocol._
import ru.rknrl.dto.CommonDTO.BuildingType
import spray.json._

object BuildingsConfigTest {
  val buildingsConfig =
    s"""
      |{
      |"tower":{
      |"regeneration":0.001,
      |"startPopulation":100,
      |"stat":${StatTest.stat}
      |},
      |"house":{
      |"regeneration":0.002,
      |"startPopulation":100,
      |"stat":${StatTest.stat}
      |},
      |"church":{
      |"regeneration":0.003,
      |"startPopulation":100,
      |"stat":${StatTest.stat}
      |}
      |}
    """.stripMargin
}

class BuildingsConfigTest extends FlatSpec with Matchers {
  "BuildingsConfig" should "be correct deserialize from json" in {
    val b = buildingsConfig.parseJson.convertTo[BuildingsConfig]

    b(BuildingType.TOWER).regeneration should be(0.001)
    b(BuildingType.HOUSE).regeneration should be(0.002)
    b(BuildingType.CHURCH).regeneration should be(0.003)
  }

  "BuildingsConfig without all buildings" should "be throw exception" in {
    a[Exception] should be thrownBy {
      s"""
        |{
        |"tower":{
        |"regeneration":0.001,
        |"startPopulation":100,
        |"stat":${StatTest.stat}
        |},
        |"church":{
        |"regeneration":0.003,
        |"startPopulation":100,
        |"stat":${StatTest.stat}
        |}
        |}
      """.stripMargin.parseJson.convertTo[BuildingsConfig]
    }
  }
}
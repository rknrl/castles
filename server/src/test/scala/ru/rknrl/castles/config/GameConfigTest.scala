package ru.rknrl.castles.config

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.config.ConfigJsonProtocol._
import ru.rknrl.castles.config.GameConfigTest.game
import ru.rknrl.castles.game.GameConfig
import spray.json._

object GameConfigTest {
  val game =
    s"""
      |{
      |    "buildings": ${BuildingsConfigTest.buildingsConfig},
      |    "levelToFactor": ${BuildingLevelToFactorTest.buildingLevelToFactor},
      |    "constants": ${ConstantsTest.constants},
      |    "fireball": ${FireballConfigTest.fireball},
      |    "volcano": ${VolcanoConfigTest.volcano},
      |    "tornado": ${TornadoConfigTest.tornado},
      |    "strengthening": ${StrengtheningConfigTest.strengthening},
      |    "shooting": ${ShootingConfigTest.shooting},
      |    "assistance": ${AssistanceConfigTest.assistance}
      |}
    """.stripMargin
}

class GameConfigTest extends FlatSpec with Matchers {
  "GameConfig" should "be correct deserialize from json" in {
    game.stripMargin.parseJson.convertTo[GameConfig] // todo
  }

  "GameConfig without buildings" should "throw Exception" in {
    a[Exception] should be thrownBy {
      s"""
      |{
      |    "levelToFactor": ${BuildingLevelToFactorTest.buildingLevelToFactor},
      |    "constants": ${ConstantsTest.constants},
      |    "fireball": ${FireballConfigTest.fireball},
      |    "volcano": ${VolcanoConfigTest.volcano},
      |    "tornado": ${TornadoConfigTest.tornado},
      |    "strengthening": ${StrengtheningConfigTest.strengthening},
      |    "shooting": ${ShootingConfigTest.shooting},
      |    "assistance": ${AssistanceConfigTest.assistance}
      |}
    """.stripMargin.parseJson.convertTo[GameConfig]
    }
  }

  "GameConfig without levelToFactor" should "throw Exception" in {
    a[Exception] should be thrownBy {
      s"""
      |{
      |    "buildings": ${BuildingsConfigTest.buildingsConfig},
      |    "constants": ${ConstantsTest.constants},
      |    "fireball": ${FireballConfigTest.fireball},
      |    "volcano": ${VolcanoConfigTest.volcano},
      |    "tornado": ${TornadoConfigTest.tornado},
      |    "strengthening": ${StrengtheningConfigTest.strengthening},
      |    "shooting": ${ShootingConfigTest.shooting},
      |    "assistance": ${AssistanceConfigTest.assistance}
      |}
    """.stripMargin.parseJson.convertTo[GameConfig]
    }
  }

  "GameConfig without constants" should "throw Exception" in {
    a[Exception] should be thrownBy {
      s"""
      |{
      |    "buildings": ${BuildingsConfigTest.buildingsConfig},
      |    "levelToFactor": ${BuildingLevelToFactorTest.buildingLevelToFactor},
      |    "fireball": ${FireballConfigTest.fireball},
      |    "volcano": ${VolcanoConfigTest.volcano},
      |    "tornado": ${TornadoConfigTest.tornado},
      |    "strengthening": ${StrengtheningConfigTest.strengthening},
      |    "shooting": ${ShootingConfigTest.shooting},
      |    "assistance": ${AssistanceConfigTest.assistance}
      |}
    """.stripMargin.parseJson.convertTo[GameConfig]
    }
  }

  "GameConfig without fireball" should "throw Exception" in {
    a[Exception] should be thrownBy {
      s"""
      |{
      |    "buildings": ${BuildingsConfigTest.buildingsConfig},
      |    "levelToFactor": ${BuildingLevelToFactorTest.buildingLevelToFactor},
      |    "constants": ${ConstantsTest.constants},
      |    "volcano": ${VolcanoConfigTest.volcano},
      |    "tornado": ${TornadoConfigTest.tornado},
      |    "strengthening": ${StrengtheningConfigTest.strengthening},
      |    "shooting": ${ShootingConfigTest.shooting},
      |    "assistance": ${AssistanceConfigTest.assistance}
      |}
    """.stripMargin.parseJson.convertTo[GameConfig]
    }
  }

  "GameConfig without volcano" should "throw Exception" in {
    a[Exception] should be thrownBy {
      s"""
      |{
      |    "buildings": ${BuildingsConfigTest.buildingsConfig},
      |    "levelToFactor": ${BuildingLevelToFactorTest.buildingLevelToFactor},
      |    "constants": ${ConstantsTest.constants},
      |    "fireball": ${FireballConfigTest.fireball},
      |    "tornado": ${TornadoConfigTest.tornado},
      |    "strengthening": ${StrengtheningConfigTest.strengthening},
      |    "shooting": ${ShootingConfigTest.shooting},
      |    "assistance": ${AssistanceConfigTest.assistance}
      |}
    """.stripMargin.parseJson.convertTo[GameConfig]
    }
  }

  "GameConfig without tornado" should "throw Exception" in {
    a[Exception] should be thrownBy {
      s"""
      |{
      |    "buildings": ${BuildingsConfigTest.buildingsConfig},
      |    "levelToFactor": ${BuildingLevelToFactorTest.buildingLevelToFactor},
      |    "constants": ${ConstantsTest.constants},
      |    "fireball": ${FireballConfigTest.fireball},
      |    "volcano": ${VolcanoConfigTest.volcano},
      |    "strengthening": ${StrengtheningConfigTest.strengthening},
      |    "shooting": ${ShootingConfigTest.shooting},
      |    "assistance": ${AssistanceConfigTest.assistance}
      |}
    """.stripMargin.parseJson.convertTo[GameConfig]
    }
  }

  "GameConfig without strengthening" should "throw Exception" in {
    a[Exception] should be thrownBy {
      s"""
      |{
      |    "buildings": ${BuildingsConfigTest.buildingsConfig},
      |    "levelToFactor": ${BuildingLevelToFactorTest.buildingLevelToFactor},
      |    "constants": ${ConstantsTest.constants},
      |    "fireball": ${FireballConfigTest.fireball},
      |    "volcano": ${VolcanoConfigTest.volcano},
      |    "tornado": ${TornadoConfigTest.tornado},
      |    "shooting": ${ShootingConfigTest.shooting},
      |    "assistance": ${AssistanceConfigTest.assistance}
      |}
    """.stripMargin.parseJson.convertTo[GameConfig]
    }
  }

  "GameConfig without shooting" should "throw Exception" in {
    a[Exception] should be thrownBy {
      s"""
      |{
      |    "buildings": ${BuildingsConfigTest.buildingsConfig},
      |    "levelToFactor": ${BuildingLevelToFactorTest.buildingLevelToFactor},
      |    "constants": ${ConstantsTest.constants},
      |    "fireball": ${FireballConfigTest.fireball},
      |    "volcano": ${VolcanoConfigTest.volcano},
      |    "tornado": ${TornadoConfigTest.tornado},
      |    "strengthening": ${StrengtheningConfigTest.strengthening},
      |    "assistance": ${AssistanceConfigTest.assistance}
      |}
    """.stripMargin.parseJson.convertTo[GameConfig]
    }
  }

  "GameConfig without assistance" should "throw Exception" in {
    a[Exception] should be thrownBy {
      s"""
      |{
      |    "buildings": ${BuildingsConfigTest.buildingsConfig},
      |    "levelToFactor": ${BuildingLevelToFactorTest.buildingLevelToFactor},
      |    "constants": ${ConstantsTest.constants},
      |    "fireball": ${FireballConfigTest.fireball},
      |    "volcano": ${VolcanoConfigTest.volcano},
      |    "tornado": ${TornadoConfigTest.tornado},
      |    "strengthening": ${StrengtheningConfigTest.strengthening},
      |    "shooting": ${ShootingConfigTest.shooting},
      |}
    """.stripMargin.parseJson.convertTo[GameConfig]
    }
  }
}
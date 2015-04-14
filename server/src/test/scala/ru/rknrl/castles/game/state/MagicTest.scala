//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import org.scalatest.{WordSpec, FreeSpec, Matchers}
import ru.rknrl.castles.game.state.Fireballs.castToFireball
import ru.rknrl.castles.game.state.Strengthening.castToStrengthening
import ru.rknrl.castles.game.state.Tornadoes.castToTornado
import ru.rknrl.castles.game.state.Volcanoes.castToVolcano
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.dto.{BuildingId, CastTornadoDTO, PlayerId, PointDTO}

// todo powerBonus работает не правильно
class MagicTest extends WordSpec with Matchers {

  "castToFireball" in {
    val fireball = castToFireball(

      cast = PlayerId(2) → PointDTO(10.1f, 20.2f),

      time = 123,

      churchesProportion = new ChurchesProportion(Map(
        PlayerId(2) → 0.5
      )),

      config = gameConfigMock(
        fireball = fireballConfigMock(
          flyDuration = 3210,
          damage = damagerConfigMock(
            powerVsUnit = 50,
            powerVsBuilding = 20,
            radius = 39,
            maxPowerBonus = 0.8
          )
        )
      )
    )

    // powerVsUnit = 50 + 0.8 * 0.5
    // powerVsBuilding = 20 + 0.8 * 0.5

    fireball.playerId shouldBe PlayerId(2)
    fireball.pos.x shouldBe (10.1 +- 0.001)
    fireball.pos.y shouldBe (20.2 +- 0.001)
    fireball.startTime shouldBe 123
    fireball.duration shouldBe 3210
    fireball.damagerConfig.powerVsUnit shouldBe (50.4 +- 0.01)
    fireball.damagerConfig.powerVsBuilding shouldBe (20.4 +- 0.01)
    fireball.damagerConfig.maxPowerBonus shouldBe 0.8
    fireball.damagerConfig.radius shouldBe 39
  }

  "castToVolcano" in {

    val volcano = castToVolcano(

      cast = PlayerId(2) → PointDTO(10.1f, 20.2f),

      time = 123,

      churchesProportion = new ChurchesProportion(Map(
        PlayerId(2) → 0.5
      )),

      config = gameConfigMock(
        volcano = volcanoConfigMock(
          duration = 3210,
          damage = damagerConfigMock(
            powerVsUnit = 50,
            powerVsBuilding = 20,
            radius = 39,
            maxPowerBonus = 0.8
          )
        )
      )
    )

    // powerVsUnit = 50 + 0.8 * 0.5
    // powerVsBuilding = 20 + 0.8 * 0.5

    volcano.playerId shouldBe PlayerId(2)
    volcano.pos.x shouldBe (10.1 +- 0.001)
    volcano.pos.y shouldBe (20.2 +- 0.001)
    volcano.startTime shouldBe 123
    volcano.duration shouldBe 3210
    volcano.damagerConfig.powerVsUnit shouldBe (50.4 +- 0.01)
    volcano.damagerConfig.powerVsBuilding shouldBe (20.4 +- 0.01)
    volcano.damagerConfig.maxPowerBonus shouldBe 0.8
    volcano.damagerConfig.radius shouldBe 39

  }

  "castToTornado" in {
    val dto = CastTornadoDTO(Seq(
      PointDTO(10.1f, 20.2f),
      PointDTO(30.1f, 40.2f),
      PointDTO(50.1f, 60.2f)
    ))

    val tornado = castToTornado(

      cast = PlayerId(2) → dto,

      time = 123,

      churchesProportion = new ChurchesProportion(Map(
        PlayerId(2) → 0.5
      )),

      config = gameConfigMock(
        tornado = tornadoConfigMock(
          duration = 3210,
          speed = 0.033,
          damage = damagerConfigMock(
            powerVsUnit = 50,
            powerVsBuilding = 20,
            radius = 39,
            maxPowerBonus = 0.8
          )
        )
      )
    )

    // powerVsUnit = 50 + 0.8 * 0.5
    // powerVsBuilding = 20 + 0.8 * 0.5

    tornado.playerId shouldBe PlayerId(2)
    tornado.points.points should have size 3
    tornado.points.points(0).x shouldBe (10.1 +- 0.001)
    tornado.points.points(0).y shouldBe (20.2 +- 0.001)
    tornado.points.points(1).x shouldBe (30.1 +- 0.001)
    tornado.points.points(1).y shouldBe (40.2 +- 0.001)
    tornado.points.points(2).x shouldBe (50.1 +- 0.001)
    tornado.points.points(2).y shouldBe (60.2 +- 0.001)
    tornado.startTime shouldBe 123
    tornado.duration shouldBe 3210
    tornado.damagerConfig.powerVsUnit shouldBe (50.4 +- 0.01)
    tornado.damagerConfig.powerVsBuilding shouldBe (20.4 +- 0.01)
    tornado.damagerConfig.maxPowerBonus shouldBe 0.8
    tornado.damagerConfig.radius shouldBe 39

  }

  "castToStrengthening" in {

    val strengthening = castToStrengthening(

      cast = PlayerId(2) → BuildingId(3),

      time = 123,

      churchesProportion = new ChurchesProportion(Map(
        PlayerId(2) → 0.5
      )),

      config = gameConfigMock(
        strengthening = strengtheningConfigMock(
          duration = 10000,
          factor = 1.5,
          maxBonusFactor = 0.5,
          maxBonusDuration = 4000
        )
      )
    )

    // duration = 10000 + 4000 * 0.5 = 12000
    // power = 1.5 + 0.5 * 0.5 = 1.75

    strengthening.buildingId shouldBe BuildingId(3)
    strengthening.startTime shouldBe 123
    strengthening.duration shouldBe 12000
    strengthening.stat shouldBe Stat(1.75, 1.75, 1)
  }

}

//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import org.scalatest.{Matchers, WordSpec}
import protos.BuildingLevel.{LEVEL_1, LEVEL_2, LEVEL_3}
import protos.BuildingType.{CHURCH, HOUSE, TOWER}
import protos.{BuildingPrototype, PlayerId}
import ru.rknrl.castles.game.state.ChurchesProportion.getChurchesProportion
import ru.rknrl.castles.kit.Mocks._

class ChurchesProportionTest extends WordSpec with Matchers {

  "apply" should {
    "ChurchesProportion(None) возвращает None" in {
      val states = new ChurchesProportion(Map.empty)
      states(None) shouldBe None
    }

    "ChurchesProportion(Some) возвращает Some(число)" in {
      val states = new ChurchesProportion(Map(
        PlayerId(1) → 0.01,
        PlayerId(2) → 0.02
      ))
      states(Some(PlayerId(1))) shouldBe Some(0.01)
    }

    "ChurchesProportion(playerId) возвращает число" in {
      val states = new ChurchesProportion(Map(
        PlayerId(1) → 0.1,
        PlayerId(2) → 0.3
      ))
      states(PlayerId(2)) shouldBe 0.3
    }

    "getChurchesProportion" should {
      "churchesProportion равно общее кол-во юнитов в церквях игрока делить на максимально возможное кол-во юнитов во всех церквях" in {
        val player1 = Some(playerMock(PlayerId(1)))
        val player2 = Some(playerMock(PlayerId(2)))

        val players = Map(
          PlayerId(1) → player1.get,
          PlayerId(2) → player2.get
        )

        val buildings = List(
          buildingMock(owner = player1, buildingPrototype = BuildingPrototype(HOUSE, LEVEL_1), count = 1),
          buildingMock(owner = player1, buildingPrototype = BuildingPrototype(CHURCH, LEVEL_1), count = 2),
          buildingMock(owner = player1, buildingPrototype = BuildingPrototype(TOWER, LEVEL_1), count = 3),
          buildingMock(owner = player1, buildingPrototype = BuildingPrototype(CHURCH, LEVEL_2), count = 4),
          buildingMock(owner = player2, buildingPrototype = BuildingPrototype(CHURCH, LEVEL_3), count = 20)
        )

        val config = gameConfigMock(
          buildings = buildingsConfigMock(
            church1 = buildingConfigMock(maxCount = 15),
            church2 = buildingConfigMock(maxCount = 25),
            church3 = buildingConfigMock(maxCount = 50)
          ))

        val proportion = getChurchesProportion(buildings, players, config)
        proportion(PlayerId(1)) shouldBe (0.06 +- 0.01)
        proportion(PlayerId(2)) shouldBe (0.22 +- 0.01)
      }

      "если нет цервей на карте" in {
        val player1 = Some(playerMock(PlayerId(1)))
        val player2 = Some(playerMock(PlayerId(2)))

        val players = Map(
          PlayerId(1) → player1.get,
          PlayerId(2) → player2.get
        )

        val buildings = List.empty[Building]

        val proportion = getChurchesProportion(buildings, players, gameConfigMock())
        proportion(PlayerId(1)) shouldBe 0
        proportion(PlayerId(2)) shouldBe 0
      }
    }

  }

}

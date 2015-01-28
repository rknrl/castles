package ru.rknrl.castles.game.objects.volcanoes

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.game.objects.PlayerStateTest
import ru.rknrl.castles.game.objects.players.PlayerId
import ru.rknrl.castles.mock.GameConfigMock
import ru.rknrl.utils.Point

class VolcanoesTest extends FlatSpec with Matchers {
  private val volcano0 = new Volcano(new PlayerId(0), new Point(100, 200), startTime = 800, duration = 2000)
  private val volcano1 = new Volcano(new PlayerId(1), new Point(500, 800), startTime = 1000, duration = 500)
  private val volcano2 = new Volcano(new PlayerId(2), new Point(600, 200), startTime = 1000, duration = 2000)

  private val config = GameConfigMock.gameConfig()

  "cast->volcanoes" should "work with empty map" in {
    Volcanoes.`casts→volcanoes`(Map.empty, 423, config, PlayerStateTest.playerStates).size should be(0)
  }

  "cast->volcanoes" should "return volcanoes" in {
    val volcano0 = new PlayerId(0) → new Point(0.1, 0.2).dto
    val volcano1 = new PlayerId(1) → new Point(0.2, 0.3).dto
    val volcano2 = new PlayerId(2) → new Point(0.3, 0.4).dto

    val volcanoes = Volcanoes.`casts→volcanoes`(Map(volcano0, volcano1, volcano2), 1200, config, PlayerStateTest.playerStates).toList

    volcanoes.size should be(3)

    volcanoes(0).playerId should be(new PlayerId(0))
    volcanoes(0).pos.x should be(0.1.toFloat.toDouble)
    volcanoes(0).pos.y should be(0.2.toFloat.toDouble)

    volcanoes(1).playerId should be(new PlayerId(1))
    volcanoes(1).pos.x should be(0.2.toFloat.toDouble)
    volcanoes(1).pos.y should be(0.3.toFloat.toDouble)

    volcanoes(2).playerId should be(new PlayerId(2))
    volcanoes(2).pos.x should be(0.3.toFloat.toDouble)
    volcanoes(2).pos.y should be(0.4.toFloat.toDouble)

  }

  "volcanoes->addMessages" should "work with empty list" in {
    Volcanoes.`volcanoes→addMessages`(List.empty, 423).size should be(0)
  }

  "volcanoes->addMessages" should "return messages" in {
    val addMessages = Volcanoes.`volcanoes→addMessages`(List(volcano0, volcano1, volcano2), 1200).toList

    addMessages.size should be(3)

    addMessages(0).volcanoDTO.getPos.getX should be(100)
    addMessages(1).volcanoDTO.getPos.getX should be(500)
    addMessages(2).volcanoDTO.getPos.getX should be(600)
  }
}
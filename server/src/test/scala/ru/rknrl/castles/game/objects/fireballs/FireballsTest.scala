package ru.rknrl.castles.game.objects.fireballs

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.game.GameConfigMock
import ru.rknrl.castles.game.objects.players.PlayerId
import ru.rknrl.utils.Point

class FireballsTest extends FlatSpec with Matchers {

  "cast->fireballs" should "work with empty map" in {
    Fireballs.`casts→fireballs`(Map.empty, GameConfigMock.gameConfig(), time = 0).size should be(0)
  }

  "cast->fireballs" should "return fireballs" in {
    val fireball1 = new PlayerId(0) → new Point(0.1, 0.2).dto
    val fireball2 = new PlayerId(1) → new Point(0.2, 0.3).dto
    val fireball3 = new PlayerId(2) → new Point(0.3, 0.4).dto

    val fireballs = Fireballs.`casts→fireballs`(Map(fireball1, fireball2, fireball3), GameConfigMock.gameConfig(), time = 0).toList

    fireballs.size should be(3)

    fireballs(0).playerId should be(new PlayerId(0))
    fireballs(0).pos.x should be(0.1.toFloat.toDouble)
    fireballs(0).pos.y should be(0.2.toFloat.toDouble)

    fireballs(1).playerId should be(new PlayerId(1))
    fireballs(1).pos.x should be(0.2.toFloat.toDouble)
    fireballs(1).pos.y should be(0.3.toFloat.toDouble)

    fireballs(2).playerId should be(new PlayerId(2))
    fireballs(2).pos.x should be(0.3.toFloat.toDouble)
    fireballs(2).pos.y should be(0.4.toFloat.toDouble)
  }

  "fireballs->addMessages" should "work with empty list" in {
    Fireballs.`fireballs→addMessages`(List.empty, time = 0).size should be(0)
  }

  "fireballs->addMessages" should "return messages" in {
    val fireball1 = new Fireball(new PlayerId(0), new Point(0.1, 0.2), duration = 1000, startTime = 0)
    val fireball2 = new Fireball(new PlayerId(1), new Point(0.2, 0.3), duration = 1000, startTime = 0)
    val fireball3 = new Fireball(new PlayerId(2), new Point(0.3, 0.4), duration = 1000, startTime = 0)

    val addMessages = Fireballs.`fireballs→addMessages`(List(fireball1, fireball2, fireball3), time = 0).toList

    addMessages.size should be(3)

    addMessages(0).fireballDTO.getX should be(0.1f)

    addMessages(1).fireballDTO.getX should be(0.2f)

    addMessages(2).fireballDTO.getX should be(0.3f)
  }
}

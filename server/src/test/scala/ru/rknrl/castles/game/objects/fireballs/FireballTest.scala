package ru.rknrl.castles.game.objects.fireballs

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.game.objects.players.PlayerId
import ru.rknrl.utils.Point

object FireballTest {
  def fireball(playerId: PlayerId = new PlayerId(77),
               x: Double = 124.124,
               y: Double = 7667.435,
               startTime: Long = 0,
               duration: Long = 1000) =
    new Fireball(playerId, new Point(x, y), startTime = startTime, duration = duration)
}

class FireballTest extends FlatSpec with Matchers {
  "dto" should "be correct" in {
    val dto = FireballTest.fireball().dto(time = 0)
    dto.getX should be(124.124f)
    dto.getY should be(7667.435f)
  }
}

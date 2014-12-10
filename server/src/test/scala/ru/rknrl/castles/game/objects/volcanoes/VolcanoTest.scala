package ru.rknrl.castles.game.objects.volcanoes

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.game.objects.players.PlayerId

object VolcanoTest {
  def volcano(playerId: PlayerId = new PlayerId(77),
              x: Double = 66.666,
              y: Double = 44.444,
              startTime: Long = 100,
              duration: Long = 50) =
    new Volcano(
      playerId,
      x,
      y,
      startTime,
      duration
    )
}

class VolcanoTest extends FlatSpec with Matchers {
  "dto" should "be correct" in {
    val currentTime = 120

    val v = VolcanoTest.volcano()

    val dto = v.dto(currentTime)
    dto.getX should be(66.666f)
    dto.getY should be(44.444f)
    dto.getMillisTillEnd should be(v.millisTillEnd(currentTime))
  }
}
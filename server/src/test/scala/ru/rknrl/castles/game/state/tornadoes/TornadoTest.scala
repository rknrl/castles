package ru.rknrl.castles.game.state.tornadoes

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.game.state.players.PlayerId
import ru.rknrl.utils.{Point, Points}

object TornadoTest {
  val playerId = new PlayerId(8)

  def tornado(points: Points = new Points(Vector(new Point(0, 0), new Point(3.14, 14.3), new Point(6, 6.34))),
              startTime: Long = 1000,
              duration: Long = 2000,
              speed: Double = 0.004) =
    new Tornado(playerId, points, startTime, duration, speed)
}

class TornadoTest extends FlatSpec with Matchers {
  "dto" should "be correct" in {
    val currentTime = 1500

    val t = TornadoTest.tornado()

    val dto = t.dto(currentTime)

    dto.getPoints(2).getY should be(6.34f)
    dto.getMillisFromStart should be(t.millisFromsStart(currentTime))
    dto.getMillisTillEnd should be(t.millisTillEnd(currentTime))
  }
}

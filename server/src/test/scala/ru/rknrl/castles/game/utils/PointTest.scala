package ru.rknrl.castles.game.utils

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.utils.Point

class PointTest extends FlatSpec with Matchers {
  "distance" should "correct" in {
    new Point(1, 1).distance(new Point(3, 4)) should be(Math.sqrt(4 + 9))
  }

  "duration" should "correct" in {
    new Point(1, 1).duration(new Point(3, 4), 0.2) should be(Math.sqrt(4 + 9) / 0.2)
  }

  "lerp" should "throw AssertException if currentTime < startTime" in {
    a[AssertionError] should be thrownBy {
      new Point(1, 1).lerp(new Point(2, 2), 200, 100, 0.4)
    }
  }

  "lerp" should "return correct x" in {
    val startPos = new Point(5, 6)
    val endPos = new Point(15, 6)
    val startTime = 10
    val speed = 1.0

    startPos.lerp(endPos, startTime, 10, speed).x should be(5)
    startPos.lerp(endPos, startTime, 10, speed).y should be(6)

    startPos.lerp(endPos, startTime, 15, speed).x should be(10)
    startPos.lerp(endPos, startTime, 15, speed).y should be(6)

    startPos.lerp(endPos, startTime, 20, speed).x should be(15)
    startPos.lerp(endPos, startTime, 20, speed).y should be(6)

    startPos.lerp(endPos, startTime, 999, speed).x should be(15)
    startPos.lerp(endPos, startTime, 999, speed).y should be(6)
  }

  "lerp" should "return correct y" in {
    val startPos = new Point(6, 5)
    val endPos = new Point(6, 15)
    val startTime = 10
    val speed = 1f

    startPos.lerp(endPos, startTime, 10, speed).x should be(6)
    startPos.lerp(endPos, startTime, 10, speed).y should be(5)

    startPos.lerp(endPos, startTime, 15, speed).x should be(6)
    startPos.lerp(endPos, startTime, 15, speed).y should be(10)

    startPos.lerp(endPos, startTime, 20, speed).x should be(6)
    startPos.lerp(endPos, startTime, 20, speed).y should be(15)

    startPos.lerp(endPos, startTime, 999, speed).x should be(6)
    startPos.lerp(endPos, startTime, 999, speed).y should be(15)
  }

  "Point.equals" should "be false with other types" in {
    val point = new Point(3.1417, 2.55443)
    (point == "point") should be(false)
  }

  "Point.equals" should "be true with same point" in {
    val point = new Point(3.1417, 2.55443)
    val point2 = new Point(3.1417, 2.55443)
    (point == point2) should be(true)
  }

  "Point.equals" should "be false with different point" in {
    (new Point(3.1417, 2.55443) == new Point(3.1418, 2.55443)) should be(false)
    (new Point(3.1417, 2.55443) == new Point(3.1417, 2.55444)) should be(false)
  }

  "Point.dto" should "be correct" in {
    val dto = new Point(3.1417, 2.55443).dto
    dto.getX should be(3.1417f)
    dto.getY should be(2.55443f)
  }
}

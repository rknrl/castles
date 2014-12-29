package ru.rknrl.utils

import ru.rknrl.dto.GameDTO.PointDTO

/**
 * todo equals with Point.as
 */
class Point(val x: Double, val y: Double) {
  def distance(endPos: Point) = {
    val dx = endPos.x - x
    val dy = endPos.y - y
    Math.sqrt(dx * dx + dy * dy)
  }

  def duration(endPos: Point, speed: Double) =
    distance(endPos) / speed

  def lerp(endPos: Point, startTime: Long, currentTime: Long, speed: Double) = {
    assert(currentTime >= startTime)

    val progress = Math.min(1, (currentTime - startTime) / duration(endPos, speed))
    new Point(x + (endPos.x - x) * progress, y + (endPos.y - y) * progress)
  }

  override def equals(obj: scala.Any): Boolean =
    obj match {
      case p: Point ⇒ x == p.x && y == p.y
      case _ ⇒ false
    }

  def dto = PointDTO.newBuilder()
    .setX(x.toFloat)
    .setY(y.toFloat)
    .build()
}
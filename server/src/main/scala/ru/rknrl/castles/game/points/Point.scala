//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.points

import ru.rknrl.dto.GameDTO.PointDTO

/**
 * todo equals with Point.as
 */
class Point private(val x: Double, val y: Double) {
  def distance(endPos: Point) = {
    val dx = endPos.x - x
    val dy = endPos.y - y
    Math.sqrt(dx * dx + dy * dy)
  }

  def duration(endPos: Point, speed: Double) =
    distance(endPos) / speed

  def lerp(endPos: Point, startTime: Long, currentTime: Long, speed: Double) = {
    val progress = Math.max(0, Math.min(1, (currentTime - startTime) / duration(endPos, speed)))
    new Point(x + (endPos.x - x) * progress, y + (endPos.y - y) * progress)
  }

  override def equals(obj: scala.Any): Boolean =
    obj match {
      case p: Point ⇒ x == p.x && y == p.y
      case _ ⇒ false
    }

  override def toString = "{" + x + "," + y + "}"

  def dto = PointDTO.newBuilder()
    .setX(x.toFloat)
    .setY(y.toFloat)
    .build()
}

object Point {
  def apply(x: Double, y: Double): Point = new Point(x, y)

  def apply(dto: PointDTO): Point = apply(dto.getX, dto.getY)
}
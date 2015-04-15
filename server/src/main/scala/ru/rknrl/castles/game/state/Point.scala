//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import ru.rknrl.dto.PointDTO

class Point(val x: Double, val y: Double) {
  def distance(that: Point) = {
    val dx = that.x - this.x
    val dy = that.y - this.y
    Math.sqrt(dx * dx + dy * dy)
  }

  def lerp(that: Point, progress: Double) = {
    val p = Math.max(0, Math.min(1, progress))
    new Point(this.x + (that.x - this.x) * p, this.y + (that.y - this.y) * p)
  }

  override def equals(obj: scala.Any): Boolean =
    obj match {
      case that: Point ⇒ this.x == that.x && this.y == that.y
      case _ ⇒ false
    }

  def dto = PointDTO(x.toFloat, y.toFloat)

  override def toString = "{" + x + "," + y + "}"
}

object Point {
  def apply(x: Double, y: Double) = new Point(x, y)

  def apply(dto: PointDTO): Point = apply(dto.x, dto.y)
}

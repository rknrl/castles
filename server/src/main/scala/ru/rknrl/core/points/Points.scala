//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.core.points

import protos.PointDTO
import ru.rknrl.Assertion

class Points(val points: Seq[Point]) {

  Assertion.check(points.size >= 2)

  lazy val distances = {
    var d = 0.0
    var ds = Vector(0.0)
    for (i ← 1 until points.size) {
      d += points(i - 1).distance(points(i))
      ds = ds :+ d
    }
    ds
  }

  lazy val totalDistance = distances.last

  def getIndex(distance: Double) = {
    var i = 0
    while (i < points.size - 1 && distances(i + 1) < distance) i += 1
    i
  }

  def pos(progress: Double) = {
    val x = Math.max(0, Math.min(1, progress))
    val distance = totalDistance * x
    val i1 = getIndex(distance)
    val i2 = i1 + 1
    val p1 = points(i1)
    val p2 = points(i2)
    if (p1 == p2)
      p1
    else {
      val lerp = (distance - distances(i1)) / p1.distance(p2)
      p1.lerp(p2, lerp)
    }
  }

  def cut(progress: Double) = {
    Assertion.check(progress > 0 && progress < 1, progress)
    val index = Math.max(1, getIndex(totalDistance * progress))
    val endPoint = pos(progress)
    new Points(points.slice(0, index) :+ endPoint)
  }

  def prolong(addDistance: Double) = {
    val p1 = points(points.size - 2)
    val p2 = points(points.size - 1)
    val n = addDistance / p1.distance(p2)
    val dx = p2.x - p1.x
    val dy = p2.y - p1.y
    val endPoint = Point(p2.x + dx * n, p2.y + dy * n)
    new Points(points :+ endPoint)
  }

  def toDistance(expectedDistance: Double) =
    if (expectedDistance > totalDistance)
      prolong(expectedDistance - totalDistance)
    else
      cut(expectedDistance / totalDistance)

  def dto = points.map(_.dto)

  override def equals(obj: scala.Any): Boolean = obj match {
    case that: Points ⇒ that.points == this.points
    case _ ⇒ false
  }
}

object Points {
  def apply(points: Point*) = new Points(points)

  def fromSeq(points: Seq[Point]) = new Points(points)

  def fromDto(dto: Seq[PointDTO]) = new Points(dto.map(Point(_)))
}

package ru.rknrl.utils

import ru.rknrl.dto.GameDTO.PointDTO

class Points(val points: Vector[Point]) {
  def this(dto: Iterable[PointDTO]) = this(Points.dtoToPoints(dto))

  assert(points.size >= 2)

  def getDurations(speed: Double) = {
    var pointsDurations = List[Double]()

    val it = points.iterator
    var totalDuration = 0.0
    var prev = points.head
    while (it.hasNext) {
      val next: Point = it.next()
      totalDuration += prev.duration(next, speed)
      pointsDurations = pointsDurations :+ totalDuration

      prev = next
    }
    pointsDurations
  }

  def getPos(currentTime: Long, speed: Double) = {
    val pointsDurations = getDurations(speed)
    val currentIndex = Points.getCurrentIndex(pointsDurations, currentTime)
    assert(currentIndex != 0)
    val oldIndex = currentIndex - 1

    val p1 = points(oldIndex)
    val p2 = points(currentIndex)
    val dx = p2.x - p1.x
    val dy = p2.y - p1.y

    val d1 = pointsDurations(oldIndex)
    val d2 = pointsDurations(currentIndex)

    val progress = (currentTime - d1) / (d2 - d1)

    new Point(p1.x + dx * progress, p1.y + dy * progress)
  }

  def dto = points.map(_.dto)
}

object Points {
  def dtoToPoints(dto: Iterable[PointDTO]) = dto.map(p ⇒ new Point(p.getX, p.getY)).toVector

  def getCurrentIndex(pointsDurations: Iterable[Double], time: Long): Int = {
    val it = pointsDurations.iterator
    var i: Int = 0
    while (it.hasNext) {
      if (time < it.next()) return i
      i += 1
    }
    return pointsDurations.size - 1
  }
}
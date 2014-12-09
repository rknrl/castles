package ru.rknrl.castles.game.utils

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.dto.GameDTO.PointDTO
import ru.rknrl.utils.{Point, Points}

class PointsTest extends FlatSpec with Matchers {
  it should "throw AssertException if points.size < 2" in {
    a[AssertionError] should be thrownBy {
      new Points(points = Vector.empty)
    }
    a[AssertionError] should be thrownBy {
      new Points(points = Vector(new Point(0, 0)))
    }
  }

  // todo: getPos

  "getDurations" should "be correct" in {
    val points = new Points(Vector(
      new Point(0, 0),
      new Point(100, 0),
      new Point(100, 300)
    ))
    val speed = 0.1

    val pointsDurations = points.getDurations(speed)

    pointsDurations.size should be(3)
    pointsDurations(0) should be(0f)
    pointsDurations(1) should be(1000f)
    pointsDurations(2) should be(4000f)
  }

  "getCurrentIndex" should "be correct" in {
    val pointsDurations = List(0.0, 100.0, 400.0, 500.0)
    Points.getCurrentIndex(pointsDurations, 0) should be(1)

    Points.getCurrentIndex(pointsDurations, 100) should be(2)
    Points.getCurrentIndex(pointsDurations, 110) should be(2)
    Points.getCurrentIndex(pointsDurations, 256) should be(2)

    Points.getCurrentIndex(pointsDurations, 400) should be(3)
    Points.getCurrentIndex(pointsDurations, 999) should be(3)
  }

  "pointsDto" should "be correct" in {
    val t = new Points(Vector(new Point(0, 0), new Point(3.14, 14.3), new Point(6, 6.34)))
    val dto: Vector[PointDTO] = t.dto

    dto(0).getX should be(0f)
    dto(0).getY should be(0f)

    dto(1).getX should be(3.14f)
    dto(1).getY should be(14.3f)

    dto(2).getX should be(6f)
    dto(2).getY should be(6.34f)
  }

  import scala.collection.JavaConverters._

  "dtoToPoints" should "correctly converted" in {
    val point1 = PointDTO.newBuilder().setX(0.44f).setY(0.55f).build
    val point2 = PointDTO.newBuilder().setX(0.55f).setY(0.66f).build
    val point3 = PointDTO.newBuilder().setX(0.66f).setY(0.77f).build
    val dtoPoints = List(point1, point2, point3).asJava
    val points = Points.dtoToPoints(dtoPoints.asScala)

    points.size should be(3)
    points(0).x should be(0.44f.toDouble)
    points(0).y should be(0.55f.toDouble)

    points(1).x should be(0.55f.toDouble)
    points(1).y should be(0.66f.toDouble)

    points(2).x should be(0.66f.toDouble)
    points(2).y should be(0.77f.toDouble)
  }
}

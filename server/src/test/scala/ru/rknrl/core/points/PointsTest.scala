//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.core.points

import org.scalatest.{Matchers, WordSpec}
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.dto.PointDTO

class PointsTest extends WordSpec with Matchers {

  "one point" in {
    a[Exception] shouldBe thrownBy {
      Points()
    }
    a[Exception] shouldBe thrownBy {
      Points(Point(0, 0))
    }
  }

  "dto" in {
    val points = Points(Point(1.1, 2.2), Point(3.3, 4.4))

    points.dto shouldBe Vector(
      PointDTO(1.1f, 2.2f),
      PointDTO(3.3f, 4.4f)
    )
  }

  "from dto" in {
    val points = Points.fromDto(Vector(
      PointDTO(1.1f, 2.2f),
      PointDTO(3.3f, 4.4f)
    ))

    points.points shouldBe Vector(
      Point(1.1f, 2.2f),
      Point(3.3f, 4.4f)
    )
  }

  def testPoints =
    Points(
      Point(0, 0),
      Point(3, 4), // distance 5
      Point(5, 4) // distance 2
    )

  "distances" in {
    testPoints.distances shouldBe Vector(0.0, 5.0, 7.0)
  }

  "totalDistance" in {
    testPoints.totalDistance shouldBe 7.0
  }

  "getIndex" in {
    val points = testPoints

    points.getIndex(0) shouldBe 0
    points.getIndex(2) shouldBe 0
    points.getIndex(5) shouldBe 0
    points.getIndex(6) shouldBe 1
    points.getIndex(7) shouldBe 1
  }

  "pos" in {
    val points = testPoints
    // totalDistance = 7

    checkPoint(points.pos(-1), Point(0, 0))
    checkPoint(points.pos(0), Point(0, 0))

    // totalDistance * 0.5 = 3.5
    // расстояние между первыми точками = 5

    checkPoint(points.pos(0.5), Point(0, 0).lerp(Point(3, 4), 3.5 / 5))

    // totalDistance * 0.8 = 5.6
    // расстояние между первыми точками = 5
    // 5.6 - distance 5 = 0.6
    // расстояние между второй и третьей точками = 2

    checkPoint(points.pos(0.8), Point(3, 4).lerp(Point(5, 4), 0.6 / 2))

    checkPoint(points.pos(1), Point(5, 4))
    checkPoint(points.pos(2), Point(5, 4))
  }

  "same points" in {
    val points = Points(Point(0, 0), Point(0, 0))
    points.pos(0.5) shouldBe Point(0, 0)
  }

  "cut" should {
    "two points" in {
      val points = Points(Point(0, 0), Point(1, 1))
      val cut = points.cut(0.5).points
      cut.size shouldBe 2
      checkPoint(cut(0), Point(0, 0))
      checkPoint(cut(1), Point(0.5, 0.5))
    }
    "two same points" in {
      val points = Points(Point(2, 1), Point(2, 1))
      val cut = points.cut(0.5).points
      cut.size shouldBe 2
      checkPoint(cut(0), Point(2, 1))
      checkPoint(cut(1), Point(2, 1))
    }

    "cut" in {
      val points = Points(Point(0, 0), Point(0, 1), Point(1, 1))
      val cut = points.cut(0.6).points
      cut.size shouldBe 2
      checkPoint(cut(0), Point(0, 0))
      checkPoint(cut(1), Point(0.2, 1))
    }

    "cut2" in {
      val points = Points(Point(-1, -1), Point(-1, 0), Point(0, 0), Point(0, 1), Point(1, 1))
      val cut = points.cut(0.8).points
      cut.size shouldBe 4
      checkPoint(cut(0), Point(-1, -1))
      checkPoint(cut(1), Point(-1, 0))
      checkPoint(cut(2), Point(0, 0))
      checkPoint(cut(3), Point(0.2, 1))
    }
  }

  "prolong" should {
    "two points" in {
      val points = Points(Point(0, 0), Point(0, 1))
      val cut = points.prolong(1.5).points
      cut.size shouldBe 3
      checkPoint(cut(0), Point(0, 0))
      checkPoint(cut(1), Point(0, 1))
      checkPoint(cut(2), Point(0, 2.5))
    }
    "prolong" in {
      val points = Points(Point(-1, -2), Point(0, 0), Point(0, 1))
      val cut = points.prolong(1.5).points
      cut.size shouldBe 4
      checkPoint(cut(0), Point(-1, -2))
      checkPoint(cut(1), Point(0, 0))
      checkPoint(cut(2), Point(0, 1))
      checkPoint(cut(3), Point(0, 2.5))
    }
  }

  "toDistance" should {
    val points = Points(Point(0, 0), Point(0, 2))
    points.toDistance(1) shouldBe Points(Point(0, 0), Point(0, 1))
    points.toDistance(4) shouldBe Points(Point(0, 0), Point(0, 2), Point(0, 4))
  }
}

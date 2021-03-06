//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.core.points

import org.scalatest.{Matchers, WordSpec}
import protos.PointDTO

class PointTest extends WordSpec with Matchers {
  "equals" in {
    Point(1.1, 2.2) shouldBe Point(1.1, 2.2)
    Point(0, 4) should not be Point(1, 4)
  }

  "dto" in {
    Point(1.1, 2.2).dto shouldBe PointDTO(1.1f, 2.2f)
  }

  "from dto" in {
    Point(PointDTO(1.1f, 2.2f)) shouldBe Point(1.1f, 2.2f)
  }

  "distance" in {
    Point(6, 1).distance(Point(3, 5)) shouldBe 5
  }

  "lerp" in {
    Point(1, 1).lerp(Point(3, 2), -1) shouldBe Point(1, 1)
    Point(1, 1).lerp(Point(3, 2), 0) shouldBe Point(1, 1)
    Point(1, 1).lerp(Point(3, 2), 0.5) shouldBe Point(2, 1.5)
    Point(1, 1).lerp(Point(3, 2), 1) shouldBe Point(3, 2)
    Point(1, 1).lerp(Point(3, 2), 2) shouldBe Point(3, 2)
  }
}

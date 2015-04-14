//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import org.scalatest.{Matchers, WordSpec}
import ru.rknrl.dto.PointDTO

class PointTest extends WordSpec with Matchers {
  "equals" in {
    Point(1.1, 2.2) shouldBe Point(1.1, 2.2)
    Point(0, 4) shouldBe Point(0, 4)
  }

  "dto" in {
    Point(1.1, 2.2).dto shouldBe PointDTO(1.1f, 2.2f)
  }

  "form dto" in {
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

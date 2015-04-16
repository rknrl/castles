//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.core

import org.scalatest.{Matchers, WordSpec}
import ru.rknrl.core.points.{Point, Points}

class MovableTest extends WordSpec with Matchers {

  "pos" in {
    val points = Points(Point(1, 2), Point(3, 4))
    val mover = Mover(
      startTime = 1,
      duration = 10,
      points = points
    )

    mover.pos(time = 5) shouldBe points.pos(mover.progress(time = 5))
  }

}

case class Mover(startTime: Long,
                 duration: Long,
                 points: Points) extends Movable

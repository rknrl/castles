//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account.state

import org.scalatest.{FreeSpec, Matchers}
import ru.rknrl.TestUtils._
import ru.rknrl.castles.game.points.Point

class IJTest extends FreeSpec with Matchers {
  "equals" in {
    checkEquals(Seq(
      () ⇒ IJ(0, 0),
      () ⇒ IJ(0, 1)
    ))
  }

  "centerXY" in {
    IJ(0, 0).centerXY shouldEqual Point(19.5, 19.5)
    IJ(11, 2).centerXY shouldEqual Point(448.5, 97.5)
  }

  "leftTopXY" in {
    IJ(0, 0).leftTopXY shouldEqual Point(0, 0)
    IJ(11, 2).leftTopXY shouldEqual Point(429, 78)
  }
}

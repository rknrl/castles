//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import org.scalatest.{Matchers, WordSpec}
import ru.rknrl.dto.{BuildingId, UnitId}

class IteratorsTest extends WordSpec with Matchers {
  "building" in {
    val iterator = new BuildingIdIterator
    iterator.next shouldBe BuildingId(0)
    iterator.next shouldBe BuildingId(1)
  }

  "unit" in {
    val iterator = new UnitIdIterator
    iterator.next shouldBe UnitId(0)
    iterator.next shouldBe UnitId(1)
  }
}

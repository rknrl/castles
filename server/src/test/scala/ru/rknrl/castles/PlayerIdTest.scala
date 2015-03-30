//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles

import org.scalatest.{FreeSpec, Matchers}
import ru.rknrl.TestUtils._
import ru.rknrl.castles.game.state.players.PlayerId

class PlayerIdTest extends FreeSpec with Matchers {
  val all = for (i ← 0 to 4) yield () ⇒ PlayerId(i)

  "equals" in {
    checkEquals(all)
  }

  "hashCode" in {
    checkHashCode(all)
  }

  "dto" in {
    PlayerId(0).dto.getId should be(0)
    PlayerId(1).dto.getId should be(1)
  }
}

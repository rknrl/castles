//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game

import org.scalatest.{FreeSpec, Matchers}
import ru.rknrl.castles.game.state.Stat

class StatTest extends FreeSpec with Matchers {
  "*" in {
    val newStat = new Stat(2, 4, 2.2) * new Stat(1.1, 1.2, 1.4)
    newStat.attack shouldBe 2.2
    newStat.defence shouldBe 4.8
    newStat.speed shouldBe 3.08
  }
}

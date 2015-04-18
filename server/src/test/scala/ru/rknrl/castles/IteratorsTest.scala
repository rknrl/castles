//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles

import org.scalatest.{Matchers, WordSpec}
import ru.rknrl.castles.game.state.{BuildingIdIterator, UnitIdIterator}
import ru.rknrl.dto.AccountType.DEV
import ru.rknrl.dto.{AccountId, BuildingId, PlayerId, UnitId}

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

  "player" in {
    val iterator = new PlayerIdIterator
    iterator.next shouldBe PlayerId(0)
    iterator.next shouldBe PlayerId(1)
  }

  "bot" in {
    val iterator = new BotIdIterator
    iterator.next shouldBe AccountId(DEV, "bot0")
    iterator.next shouldBe AccountId(DEV, "bot1")
  }

  "gameId" in {
    val iterator = new GameIdIterator
    iterator.next shouldBe "game0"
    iterator.next shouldBe "game1"
  }
}

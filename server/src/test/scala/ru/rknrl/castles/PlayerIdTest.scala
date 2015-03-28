//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles

import org.scalatest.{FunSuite, Matchers}
import ru.rknrl.castles.game.state.players.PlayerId

class PlayerIdTest extends FunSuite with Matchers {
  test("playerId == такому же playerId") {
    val id = new PlayerId(0)
    id shouldEqual id
    new PlayerId(0) shouldEqual new PlayerId(0)
    new PlayerId(1) shouldEqual new PlayerId(1)
  }

  test("playerId != отличному playerId") {
    new PlayerId(0) shouldNot equal(new PlayerId(1))
    new PlayerId(1) shouldNot equal(new PlayerId(0))
  }

  test("playerId should NOT be equals with diff types") {
    new PlayerId(0) shouldNot equal(0)
    new PlayerId(1) shouldNot equal(1)
  }

  test("разные playerId корректно работают в сэте") {
    var set = Set.empty[PlayerId]
    set = set + new PlayerId(0) + new PlayerId(1)

    set.size should be(2)
    set.contains(new PlayerId(0)) should be(true)
    set.contains(new PlayerId(1)) should be(true)
    set.contains(new PlayerId(2)) should be(false)
  }

  test("одинаковые playerId корректно работают в сэте") {
    var set = Set.empty[PlayerId]
    set = set + new PlayerId(0) + new PlayerId(0)

    set.size should be(1)
    set.contains(new PlayerId(0)) should be(true)
    set.contains(new PlayerId(1)) should be(false)
    set.contains(new PlayerId(2)) should be(false)
  }

  test("разные playerId корректно работают в мапе") {
    var set = Map.empty[PlayerId, Boolean]
    set = set + (new PlayerId(0) → true) + (new PlayerId(1) → true)

    set.size should be(2)
    set.contains(new PlayerId(0)) should be(true)
    set.contains(new PlayerId(1)) should be(true)
    set.contains(new PlayerId(2)) should be(false)
  }

  test("одинаковые playerId корректно работают в мапе") {
    var set = Map.empty[PlayerId, Boolean]
    set = set + (new PlayerId(0) → true) + (new PlayerId(0) → true)

    set.size should be(1)
    set.contains(new PlayerId(0)) should be(true)
    set.contains(new PlayerId(1)) should be(false)
    set.contains(new PlayerId(2)) should be(false)
  }

  test("playerId выдает корректный dto") {
    new PlayerId(0).dto.getId should be(0)
    new PlayerId(1).dto.getId should be(1)
  }
}

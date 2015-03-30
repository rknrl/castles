//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account.state

import org.scalatest.{FreeSpec, Matchers}
import ru.rknrl.dto.CommonDTO.ItemType._

class ItemTest extends FreeSpec with Matchers {
  "negative count" in {
    a[Exception] shouldBe thrownBy {
      new Item(FIREBALL, -1)
    }
  }

  "+" in {
    val newItem = new Item(TORNADO, 10) + 5
    newItem.count shouldBe 15
  }

  "-" in {
    val newItem = new Item(TORNADO, 10) + (-5)
    newItem.count shouldBe 5
  }

  "При начислении отрицательных значений " +
    "мы не можем создать предмет с отрицательным кол-вом" +
    "т.к убавить предмет могут одновременно админ и игра" in {
    val newItem = new Item(TORNADO, 10) + (-999)
    newItem.count shouldBe 0
  }

  "dto" in {
    val dto = new Item(VOLCANO, 18).dto
    dto.getType shouldBe VOLCANO
    dto.getCount shouldBe 18
  }
}

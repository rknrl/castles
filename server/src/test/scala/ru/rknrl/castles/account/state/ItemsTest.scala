//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account.state

import org.scalatest.{FreeSpec, Matchers}
import ru.rknrl.dto.CommonDTO.ItemType
import ru.rknrl.dto.CommonDTO.ItemType._

class ItemsTest extends FreeSpec with Matchers {

  def items = new Items(Map(
    FIREBALL → new Item(FIREBALL, 1),
    STRENGTHENING → new Item(STRENGTHENING, 2),
    TORNADO → new Item(TORNADO, 2),
    VOLCANO → new Item(VOLCANO, 3),
    ASSISTANCE → new Item(ASSISTANCE, 0)
  ))

  def checkEquals(newItems: Items, without: Option[ItemType]): Unit =
    for ((itemType, item) ← items.items
         if without.isEmpty || itemType != without.get;
         newItem = newItems(itemType))
      newItem.count shouldBe item.count

  "Не все предметы" in {
    a[Exception] shouldBe thrownBy {
      new Items(Map(
        FIREBALL → new Item(FIREBALL, 1),
        STRENGTHENING → new Item(STRENGTHENING, 2)
      ))
    }
  }

  "add" in {
    val newItems = items.add(VOLCANO, 7)
    newItems(VOLCANO).count shouldBe 10
    checkEquals(newItems, Some(VOLCANO))
  }
}

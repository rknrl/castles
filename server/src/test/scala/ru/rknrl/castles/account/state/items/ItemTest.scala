package ru.rknrl.castles.account.state.items

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.account.state.Item
import ru.rknrl.dto.CommonDTO.ItemType

class ItemTest extends FlatSpec with Matchers {
  it should "throw AssertionError if count < 0" in {
    a[AssertionError] should be thrownBy {
      new Item(ItemType.FIREBALL, -1)
    }
  }

  "add" should "change count & not change type" in {
    new Item(ItemType.FIREBALL, 1).add(-1).count should be(0)
    new Item(ItemType.FIREBALL, 1).add(-1).itemType should be(ItemType.FIREBALL)

    new Item(ItemType.TORNADO, 1).add(3).count should be(4)
    new Item(ItemType.TORNADO, 1).add(3).itemType should be(ItemType.TORNADO)
  }

  "dto" should "be correct" in {
    val dto = new Item(ItemType.TORNADO, 3).dto
    dto.getType should be(ItemType.TORNADO)
    dto.getCount should be(3)
  }
}

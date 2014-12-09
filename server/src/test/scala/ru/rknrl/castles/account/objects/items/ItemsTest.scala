package ru.rknrl.castles.account.objects.items

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.account.objects.{Item, Items}
import ru.rknrl.dto.AccountDTO.{ItemDTO, ItemsDTO}
import ru.rknrl.dto.CommonDTO.ItemType

object ItemsTest {
  val fireball = ItemType.FIREBALL → new Item(ItemType.FIREBALL, 100)
  val strengthening = ItemType.STRENGTHENING → new Item(ItemType.STRENGTHENING, 0)
  val volcano = ItemType.VOLCANO → new Item(ItemType.VOLCANO, 300)
  val tornado = ItemType.TORNADO → new Item(ItemType.TORNADO, 1)
  val assistance = ItemType.ASSISTANCE → new Item(ItemType.ASSISTANCE, 2)
  val all = Map(fireball, strengthening, volcano, tornado, assistance)

  val items = new Items(all)
}

class ItemsTest extends FlatSpec with Matchers {

  import ru.rknrl.castles.account.objects.items.ItemsTest._

  it should "throw AssertionError if not contain all items" in {
    a[AssertionError] should be thrownBy {
      new Items(Map(fireball, tornado))
    }
  }

  "add" should "change item & not change other" in {
    val updated = items.add(ItemType.VOLCANO, -5)
    updated.items.size should be(5)
    updated.items(ItemType.VOLCANO).count should be(295)
    updated.items(ItemType.FIREBALL).count should be(100)
  }

  "dto" should "be correct" in {
    checkDto(items, items.dto)
  }

  def checkDto(items: Items, dto: ItemsDTO) {
    dto.getItemsCount should be(5)

    getItem(ItemType.FIREBALL).getCount should be(100)
    getItem(ItemType.STRENGTHENING).getCount should be(0)
    getItem(ItemType.VOLCANO).getCount should be(300)
    getItem(ItemType.TORNADO).getCount should be(1)
    getItem(ItemType.ASSISTANCE).getCount should be(2)

    def getItem(itemType: ItemType): ItemDTO = {
      for (i ← 0 until dto.getItemsCount)
        if (dto.getItems(i).getType == itemType)
          return dto.getItems(i)
      throw new IllegalStateException()
    }
  }
}

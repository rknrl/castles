package ru.rknrl.castles.account.state

import ru.rknrl.dto.AccountDTO.ItemDTO
import ru.rknrl.dto.CommonDTO.ItemType

class Item(val itemType: ItemType,
           val count: Int) {

  assert(count >= 0)

  def add(value: Int) = new Item(itemType, Math.max(0, count + value))

  def dto = ItemDTO.newBuilder()
    .setType(itemType)
    .setCount(count)
    .build()
}

object Item {
  def fromDto(dto: ItemDTO) = new Item(dto.getType, dto.getCount)
}

class Items(val items: Map[ItemType, Item]) {

  for (itemType ← ItemType.values()) assert(items.contains(itemType))

  def add(itemType: ItemType, value: Int) =
    new Items(items.updated(itemType, items(itemType).add(value)))

  def dto = for ((itemType, item) ← items) yield item.dto
}

object Items {
  def fromDto(dto: Iterable[ItemDTO]) = {
    val items = for (itemDto ← dto) yield itemDto.getType → Item.fromDto(itemDto)
    new Items(items.toMap)
  }
}

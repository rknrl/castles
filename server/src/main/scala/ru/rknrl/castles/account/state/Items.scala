//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account.state

import ru.rknrl.Assertion
import ru.rknrl.dto.AccountDTO.ItemDTO
import ru.rknrl.dto.CommonDTO.ItemType

class Item(val itemType: ItemType,
           val count: Int) {

  Assertion.check(count >= 0)

  def dto = ItemDTO.newBuilder
    .setType(itemType)
    .setCount(count)
    .build
}

object Item {
  def apply(dto: ItemDTO) = new Item(dto.getType, dto.getCount)
}

class Items(val items: Map[ItemType, Item]) {
  for (itemType ← ItemType.values) Assertion.check(items.contains(itemType))

  def apply(itemType: ItemType) = items(itemType)

  def updated(itemType: ItemType, item: Item) =
    new Items(items.updated(itemType, item))

  def dto = items.values.map(_.dto)
}

object Items {
  def apply(dto: Iterable[ItemDTO]) = {
    val items = for (itemDto ← dto) yield itemDto.getType → Item(itemDto)
    new Items(items.toMap)
  }
}

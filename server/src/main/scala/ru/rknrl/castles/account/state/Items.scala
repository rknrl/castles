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

  def +(value: Int) = new Item(itemType, Math.max(0, count + value))

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

  def add(itemType: ItemType, value: Int) =
    new Items(items.updated(itemType, items(itemType) + value))

  def dto = items.values.map(_.dto)
}

object Items {
  def apply(dto: Iterable[ItemDTO]) = {
    val items = for (itemDto ← dto) yield itemDto.getType → Item(itemDto)
    new Items(items.toMap)
  }
}

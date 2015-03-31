//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account.state

import ru.rknrl.dto.AccountDTO.ItemDTO
import ru.rknrl.dto.CommonDTO.ItemType

object Item {
  type Items = Map[ItemType, ItemDTO]

  def apply(itemType: ItemType, count: Int) =
    ItemDTO.newBuilder
      .setType(itemType)
      .setCount(count)
      .build
}

object Items {
  def apply(items: Iterable[ItemDTO]) =
    items.map(i ⇒ i.getType → i).toMap
}
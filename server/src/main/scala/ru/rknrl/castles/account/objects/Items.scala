package ru.rknrl.castles.account.objects

import ru.rknrl.dto.AccountDTO.{ItemDTO, ItemsDTO}
import ru.rknrl.dto.CommonDTO.ItemType

import scala.collection.JavaConverters._

class Item(val itemType: ItemType,
           val count: Int) {

  assert(count >= 0)

  def add(value: Int) = new Item(itemType, count + value)

  def dto = ItemDTO.newBuilder()
    .setType(itemType)
    .setCount(count)
    .build()
}

class Items(val items: Map[ItemType, Item]) {

  for (itemType ← ItemType.values()) assert(items.contains(itemType))

  def add(itemType: ItemType, value: Int) =
    new Items(items.updated(itemType, items(itemType).add(value)))

  private def itemsDto = for ((itemType, item) ← items) yield item.dto

  def dto = ItemsDTO.newBuilder()
    .addAllItems(itemsDto.asJava)
    .build()
}

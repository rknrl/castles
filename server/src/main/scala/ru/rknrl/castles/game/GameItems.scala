package ru.rknrl.castles.game

import ru.rknrl.castles.account.objects.Items
import ru.rknrl.castles.game.objects.players.PlayerId
import ru.rknrl.castles.rmi.UpdateItemStatesMsg
import ru.rknrl.dto.CommonDTO.ItemType
import ru.rknrl.dto.GameDTO.{ItemStateDTO, ItemsStateDTO}

import scala.collection.JavaConverters._

class GameItemState(val itemType: ItemType,
                    val count: Int,
                    val lastUseTime: Long) {
  assert(count >= 0)

  def use(time: Long) = new GameItemState(itemType, count - 1, lastUseTime = time)

  def differentWith(state: GameItemState) =
    lastUseTime != state.lastUseTime || count != state.count

  def dto(time: Long, config: GameConfig) = {
    val millisTillCooldownEnd: Long = Math.max(0, config.constants.itemCooldown - (time - lastUseTime))
    ItemStateDTO.newBuilder()
      .setItemType(itemType)
      .setCount(count)
      .setMillisTillCooldownEnd(millisTillCooldownEnd.toInt)
      .setCooldownDuration(config.constants.itemCooldown.toInt)
      .build()
  }
}

class GameItemsState(val playerId: PlayerId,
                     val items: Map[ItemType, GameItemState]) {

  def use(itemType: ItemType, time: Long) = {
    val newItem = items(itemType).use(time)
    new GameItemsState(playerId, items = items.updated(itemType, newItem))
  }

  def differentWith(oldState: GameItemsState): Boolean = {
    for ((itemType, state) ← items)
      if (state differentWith oldState.items(itemType)) return true

    false
  }

  private def itemsDto(time: Long, config: GameConfig) =
    for ((itemType, state) ← items)
    yield state.dto(time, config)

  def dto(time: Long, config: GameConfig) =
    ItemsStateDTO.newBuilder()
      .addAllItems(itemsDto(time, config).asJava)
      .build()

  def usedItems =
    for ((itemType, state) ← items) yield itemType → 0
}

object GameItems {
  private def initMap(items: Items) =
    for ((itemType, item) ← items.items)
    yield itemType → new GameItemState(itemType, item.count, 0)

  def init(playerId: PlayerId, items: Items) =
    new GameItemsState(playerId, initMap(items))

  def getUpdateItemsStatesMessages(oldItems: GameItems, item: GameItems, config: GameConfig, time: Long) =
    for ((playerId, state) ← item.states;
         oldState = oldItems.states(playerId)
         if state differentWith oldState
    ) yield new PersonalMessage(playerId, new UpdateItemStatesMsg(state.dto(time, config)))
}

class GameItems(val states: Map[PlayerId, GameItemsState]) {
  def applyCasts(casts: Map[PlayerId, _], itemType: ItemType, time: Long) =
    new GameItems(
      for ((playerId, state) ← states)
      yield
        if (casts.contains(playerId))
          playerId → state.use(itemType, time)
        else
          playerId → state
    )

  def canCast(playerId: PlayerId, itemType: ItemType, config: GameConfig, time: Long) =
    time - states(playerId).items(itemType).lastUseTime >= config.constants.itemCooldown

  def assertCasts(casts: Map[PlayerId, _], itemType: ItemType, config: GameConfig, time: Long) =
    for ((playerId, _) ← casts)
      assert(canCast(playerId, itemType, config, time))

  def dto(playerId: PlayerId, time: Long, config: GameConfig) = states(playerId).dto(time, config)
}
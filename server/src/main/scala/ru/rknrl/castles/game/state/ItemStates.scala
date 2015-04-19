//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import ru.rknrl.Assertion
import ru.rknrl.castles.account.AccountState.Items
import ru.rknrl.castles.game.GameConfig
import ru.rknrl.castles.rmi.B2C.UpdateItemStates
import ru.rknrl.dto._

case class ItemState(itemType: ItemType,
                     count: Int,
                     lastUseTime: Long,
                     useCount: Int) {
  Assertion.check(count >= 0)

  def use(time: Long) = new ItemState(itemType, count - 1, lastUseTime = time, useCount + 1)

  def dto(time: Long, config: GameConfig) = {
    val duration = config.constants.itemCooldown
    val millisFromStart = Math.min(duration, time - lastUseTime)

    ItemStateDTO(
      itemType = itemType,
      count = count,
      millisFromStart = millisFromStart.toInt,
      cooldownDuration = duration.toInt
    )
  }
}

class ItemStates(val items: Map[ItemType, ItemState]) {

  def use(itemType: ItemType, time: Long) = {
    val newItem = items(itemType).use(time)
    new ItemStates(items = items.updated(itemType, newItem))
  }

  def usedItems =
    for ((itemType, state) ← items)
      yield itemType → state.useCount

  private def itemsDto(time: Long, config: GameConfig) =
    for ((itemType, state) ← items)
      yield state.dto(time, config)

  def dto(playerId: PlayerId, time: Long, config: GameConfig) =
    ItemStatesDTO(
      playerId = playerId,
      items = itemsDto(time, config).toSeq
    )

  override def equals(obj: scala.Any): Boolean = obj match {
    case that: ItemStates ⇒ this.items == that.items
    case _ ⇒ false
  }
}

object GameItems {
  private def initMap(items: Items) =
    for ((itemType, count) ← items)
      yield itemType → new ItemState(itemType, count, lastUseTime = 0, useCount = 0)

  def init(items: Items) =
    new ItemStates(initMap(items))

  def getUpdateItemsStatesMessages(oldItems: GameItems, item: GameItems, config: GameConfig, time: Long) =
    for ((playerId, state) ← item.states;
         oldState = oldItems.states(playerId)
         if state != oldState
    ) yield UpdateItemStates(state.dto(playerId, time, config))
}

class GameItems(val states: Map[PlayerId, ItemStates]) {
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
    states(playerId).items(itemType).count > 0 &&
      time - states(playerId).items(itemType).lastUseTime >= config.constants.itemCooldown

  def checkCasts[T](casts: Map[PlayerId, T], itemType: ItemType, config: GameConfig, time: Long) =
    casts.filter { case (playerId, _) ⇒ canCast(playerId, itemType, config, time) }

  def dto(playerId: PlayerId, time: Long, config: GameConfig) = states(playerId).dto(playerId, time, config)

  override def equals(obj: scala.Any): Boolean =
    obj match {
      case that: GameItems ⇒ this.states == that.states
      case _ ⇒ false
    }
}
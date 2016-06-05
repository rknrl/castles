//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.matchmaking

import protos.AccountType.DEV
import protos.{AccountId, Item, PlayerId}
import ru.rknrl.IntIterator
import ru.rknrl.castles.Config
import ru.rknrl.castles.game.init.{GameMaps, GameStateInit}
import ru.rknrl.castles.game.state.{GameState, Player}
import ru.rknrl.castles.matchmaking.GameCreator.NewGame
import ru.rknrl.castles.matchmaking.MatchMaking.GameOrder
import ru.rknrl.castles.matchmaking.Matcher.MatchedGameOrders
import ru.rknrl.core.Stat

object GameCreator {

  case class NewGame(orders: Seq[GameOrder],
                     isTutor: Boolean,
                     gameState: GameState)

}

class BotIdIterator extends IntIterator {
  def next = AccountId(DEV, "bot" + nextInt)
}

class PlayerIdIterator extends IntIterator {
  def next = PlayerId(nextInt)
}

class GameCreator(gameMaps: GameMaps,
                  config: Config) {

  val botIdIterator = new BotIdIterator

  def newGame(matched: MatchedGameOrders, time: Long) = {
    val big = matched.playersCount == 4

    val humanOrder = matched.orders.head
    val botsCount = matched.playersCount - matched.orders.size
    val botsOrders = createBotOrders(botsCount, humanOrder)

    val orders = matched.orders ++ botsOrders
    val players = ordersToPlayers(orders, matched.isTutor)

    val gameMap = if (matched.isTutor) gameMaps.tutor(big) else gameMaps.random(big)

    val gameState = GameStateInit.init(
      time = time,
      players = players.toList,
      big = big,
      isTutor = matched.isTutor,
      config = config.game,
      gameMap = gameMap
    )

    NewGame(orders, matched.isTutor, gameState)
  }

  def createBotOrders(botsCount: Int, humanOrder: GameOrder) =
    for (i ← 0 until botsCount) yield {
      val accountId = botIdIterator.next
      GameOrder(
        accountId = accountId,
        deviceType = humanOrder.deviceType,
        userInfo = config.botUserInfo(accountId, i),
        botAccountState(humanOrder),
        rating = humanOrder.rating,
        isBot = true
      )
    }

  def botAccountState(humanOrder: GameOrder) =
    humanOrder.accountState.copy(
      items = botItems(humanOrder.accountState.items)
    )

  def botItems(humanItems: Seq[Item]) =
    humanItems.map(item ⇒ item.copy(count = item.count * 2))

  val tutorHumanStat = new Stat(attack = 3, defence = 3, speed = 1)
  val tutorBotStat = new Stat(attack = 0.3, defence = 0.3, speed = 1)

  def ordersToPlayers(orders: Iterable[GameOrder], isTutor: Boolean) = {
    val playerIdIterator = new PlayerIdIterator
    for (order ← orders) yield {
      val stat = if (isTutor) {
        if (order.isBot) tutorBotStat else tutorHumanStat
      } else {
        config.account.skillsToStat(order.accountState.skills)
      }
      val playerId = playerIdIterator.next
      Player(
        playerId,
        order.accountId,
        order.userInfo,
        order.accountState.slots,
        stat,
        order.accountState.items,
        order.isBot
      )
    }
  }
}

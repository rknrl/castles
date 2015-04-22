//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.matchmaking

import ru.rknrl.castles.Config
import ru.rknrl.castles.game.init.{GameMaps, GameStateInit}
import ru.rknrl.castles.game.state.{GameState, Player}
import ru.rknrl.castles.matchmaking.GameCreator.NewGame
import ru.rknrl.castles.matchmaking.Matcher.MatchedGameOrders
import ru.rknrl.castles.matchmaking.NewMatchmaking.GameOrder

object GameCreator {

  case class NewGame(orders: Seq[GameOrder],
                     isTutor: Boolean,
                     gameState: GameState)

}

class GameCreator(gameMaps: GameMaps,
                  config: Config) {

  val botIdIterator = new BotIdIterator

  def newGame(matched: MatchedGameOrders) = {
    val big = matched.playersCount == 4

    val humanOrder = matched.orders.head
    val botsCount = matched.playersCount - matched.orders.size
    val botsOrders = createBotOrders(botsCount, humanOrder)

    val orders = matched.orders ++ botsOrders
    val players = ordersToPlayers(orders)

    val gameMap = if (matched.isTutor) gameMaps.tutor(big) else gameMaps.random(big)

    val gameState = GameStateInit.init(
      time = 0,
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
        isBot = true
      )
    }

  def botAccountState(humanOrder: GameOrder) = humanOrder.accountState

  def ordersToPlayers(orders: Iterable[GameOrder]) = {
    val playerIdIterator = new PlayerIdIterator
    for (order ← orders) yield {
      val stat = config.account.skillsToStat(order.accountState.skills)
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

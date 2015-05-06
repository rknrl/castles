//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.matchmaking

import ru.rknrl.IdIterator
import ru.rknrl.castles.Config
import ru.rknrl.castles.account.AccountState
import ru.rknrl.castles.account.AccountState._
import ru.rknrl.castles.game.init.{GameMaps, GameStateInit}
import ru.rknrl.castles.game.state.{GameState, Player}
import ru.rknrl.castles.matchmaking.GameCreator.NewGame
import ru.rknrl.castles.matchmaking.MatchMaking.GameOrder
import ru.rknrl.castles.matchmaking.Matcher.MatchedGameOrders
import ru.rknrl.core.Stat
import ru.rknrl.dto.AccountType.DEV
import ru.rknrl.dto.{AccountId, PlayerId}

object GameCreator {

  case class NewGame(orders: Seq[GameOrder],
                     isTutor: Boolean,
                     gameState: GameState)

}

class BotIdIterator extends IdIterator {
  def next = AccountId(DEV, "bot" + nextInt)
}

class PlayerIdIterator extends IdIterator {
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
        isBot = true
      )
    }

  def botAccountState(humanOrder: GameOrder) =
    new AccountState(
      slots = humanOrder.accountState.slots,
      skills = humanOrder.accountState.skills,
      items = botItems(humanOrder.accountState.items),
      gold = humanOrder.accountState.gold,
      rating = humanOrder.accountState.rating,
      gamesCount = humanOrder.accountState.gamesCount
    )

  def botItems(humanItems: Items) =
    humanItems.mapValues(count ⇒ count * 2)

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

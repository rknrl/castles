package ru.rknrl.castles

import akka.actor.Props
import ru.rknrl.base.MatchMaking.{TopItem, GameOrder}
import ru.rknrl.base._
import ru.rknrl.castles.account.state.{Item, Items}
import ru.rknrl.castles.bot.Bot
import ru.rknrl.castles.game._
import ru.rknrl.castles.game.state.players.Player
import ru.rknrl.dto.CommonDTO.{DeviceType, UserInfoDTO}

import scala.concurrent.duration.FiniteDuration

class CastlesMatchMaking(interval: FiniteDuration, top: List[TopItem], gameConfig: GameConfig) extends MatchMaking(interval, top, gameConfig) {

  override def tryCreateGames(gameOrders: List[GameOrder]) = {
    val (smallGameOrders, bigGameOrders) = gameOrders.span(_.deviceType == DeviceType.PHONE)

    createGames(big = false, playersCount = 2, smallGameOrders) ++
      createGames(big = true, playersCount = 4, bigGameOrders)
  }

  private def createGames(big: Boolean, playersCount: Int, orders: List[GameOrder]) = {
    var sorted = orders.sortBy(_.rating)(Ordering.Double.reverse)
    var createdGames = List.empty[GameInfo]

    while (sorted.size > playersCount) {
      createdGames = createdGames :+ createGame(big, sorted.take(playersCount))
      sorted = sorted.drop(playersCount)
    }

    if (sorted.size > 0)
      createdGames = createdGames :+ createGameWithBot(big, playersCount, sorted)

    createdGames
  }

  private val botIdIterator = new BotIdIterator

  private def createGameWithBot(big: Boolean, playerCount: Int, orders: List[GameOrder]) = {
    val botsCount = if (big) playerCount - orders.size else playerCount - orders.size
    assert(botsCount >= 1, botsCount)

    val order = orders.head
    var result = orders

    for (i ← 0 until botsCount) {
      val accountId = botIdIterator.next
      val bot = context.actorOf(Props(classOf[Bot], accountId, gameConfig), accountId.id)

      val botOrder = new GameOrder(accountId, DeviceType.CANVAS, botUserInfo(accountId, i), order.slots, order.skills, botItems(order.items), order.rating, order.gamesCount, isBot = true)
      result = result :+ botOrder
      placeGameOrder(botOrder, bot)
    }

    createGame(big, result)
  }

  private def botItems(playerItems: Items) =
    new Items(playerItems.items.map {
      case (itemType, item) ⇒ (itemType, new Item(itemType, item.count * 2))
    })

  private def botUserInfo(accountId: AccountId, number: Int) =
    UserInfoDTO.newBuilder()
      .setAccountId(accountId.dto)
      .setFirstName("Бот")
      .setLastName(number.toString)
      .setPhoto96("1")
      .setPhoto256("1")
      .build()

  private val gameIdIterator = new GameIdIterator

  private def createGame(big: Boolean, orders: Iterable[GameOrder]) = {
    val playerIdIterator = new PlayerIdIterator

    val players = for (order ← orders) yield {
      val playerId = playerIdIterator.next
      playerId → new Player(playerId, order.accountId, order.userInfo, order.slots, order.skills, order.items, isBot = order.isBot)
    }

    val game = context.actorOf(Props(classOf[CastlesGame], players.toMap, big, gameConfig, self), gameIdIterator.next)

    new GameInfo(game, orders)
  }
}

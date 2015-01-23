package ru.rknrl.castles

import akka.actor.Props
import ru.rknrl.base.MatchMaking.GameOrder
import ru.rknrl.base._
import ru.rknrl.castles.account.objects.{Item, Items}
import ru.rknrl.castles.bot.Bot
import ru.rknrl.castles.game._
import ru.rknrl.castles.game.objects.players.Player
import ru.rknrl.dto.CommonDTO.{DeviceType, UserInfoDTO}

import scala.concurrent.duration.FiniteDuration

class CastlesMatchMaking(interval: FiniteDuration, gameConfig: GameConfig) extends MatchMaking(interval, gameConfig) {

  private def isBigGame(deviceType: DeviceType) = deviceType != DeviceType.PHONE

  private def friendlyDevices(a: DeviceType, b: DeviceType) =
    (a == DeviceType.PHONE && b == DeviceType.PHONE) || (a != DeviceType.PHONE && b != DeviceType.PHONE)

  /**
   * Создать игры из имеющихся заявок
   * Если заявок две - создаем игру между этими двумя игроками
   * Если заявка одна - создаем игру с ботом
   */
  override def tryCreateGames(gameOrders: List[GameOrder]) =
    if (gameOrders.size == 2 && friendlyDevices(gameOrders(0).deviceType, gameOrders(1).deviceType)) {
      val order1 = gameOrders(0)
      val order2 = gameOrders(1)
      List(createGame(isBigGame(order1.deviceType), List(order1, order2)))
    } else if (gameOrders.size == 1) {
      val order = gameOrders(0)
      List(createGameWithBot(order))
    } else
      List.empty

  private val botIdIterator = new BotIdIterator

  private def createGameWithBot(order: GameOrder) = {
    val bigGame = isBigGame(order.deviceType)

    val botsCount = if (bigGame) 3 else 1

    var orders = List(order)

    for (i ← 0 until botsCount) {
      val accountId = botIdIterator.next
      val bot = context.actorOf(Props(classOf[Bot], accountId, gameConfig), accountId.id)

      val botOrder = new GameOrder(accountId, DeviceType.CANVAS, botUserInfo(accountId, i), order.startLocation, order.skills, botItems(order.items), isBot = true)
      orders = orders :+ botOrder
      placeGameOrder(botOrder, bot)
    }

    createGame(bigGame, orders)
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
      playerId → new Player(playerId, order.accountId, order.userInfo, order.startLocation, order.skills, order.items, isBot = order.isBot)
    }

    val game = context.actorOf(Props(classOf[CastlesGame], players.toMap, big, gameConfig, self), gameIdIterator.next)

    new GameInfo(game, orders)
  }
}

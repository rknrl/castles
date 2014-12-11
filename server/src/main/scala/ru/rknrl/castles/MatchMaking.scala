package ru.rknrl.castles

import akka.actor.{Actor, ActorRef, Props}
import ru.rknrl.castles.MatchMaking._
import ru.rknrl.castles.account.objects._
import ru.rknrl.castles.bot.Bot
import ru.rknrl.castles.game.Game.StopGame
import ru.rknrl.castles.game._
import ru.rknrl.castles.game.objects.players.{Player, PlayerId}
import ru.rknrl.dto.AuthDTO.{AccountType, DeviceType}
import ru.rknrl.utils.IdIterator

import scala.collection.mutable
import scala.concurrent.duration._

object MatchMaking {

  class GameOrder(val externalAccountId: AccountId,
                  val deviceType: DeviceType,
                  val accountRef: ActorRef,
                  val startLocation: StartLocation,
                  val skills: Skills,
                  val items: Items,
                  val isBot: Boolean)

  // account -> matchmaking

  case class PlaceGameOrder(gameOrder: GameOrder)

  case class InGame(externalAccountId: AccountId)

  // matchmaking -> account

  case class InGameResponse(gameRef: Option[ActorRef])

  case class ConnectToGame(game: ActorRef)

  // game -> matchmaking

  case class Leaved(externalAccountId: AccountId)

  case object GameOver

}

class BotIdIterator extends IdIterator {
  def next = new AccountId(AccountType.DEV, "bot" + nextInt)
}

class GameIdIterator extends IdIterator {
  def next = "game" + nextInt
}

class PlayerIdIterator extends IdIterator {
  def next = new PlayerId(nextInt)
}

class MatchMaking(interval: FiniteDuration, gameConfig: GameConfig) extends Actor {

  class GameInfo(val gameRef: ActorRef,
                 val externalAccountIds: Iterable[AccountId])

  private val gameOrders = mutable.ListBuffer[GameOrder]()

  private val playerToGameInfo = mutable.Map[AccountId, GameInfo]()

  case object TryCreateGames

  import context.dispatcher

  context.system.scheduler.schedule(0 seconds, interval, self, TryCreateGames)

  def receive = {
    /**
     * Аккаунт спрашивает находится ли он сейчас в игре?
     * В ответ отпарвялем InGameState
     */
    case InGame(externalAccountId) ⇒
      val gameInfo = playerToGameInfo.get(externalAccountId)
      if (gameInfo.isEmpty)
        sender ! InGameResponse(None)
      else
        sender ! InGameResponse(Some(gameInfo.get.gameRef))

    /**
     * Аккаунт присылает заявку на игру
     */
    case PlaceGameOrder(gameOrder) ⇒
      assert(playerToGameInfo.get(gameOrder.externalAccountId).isEmpty)

      gameOrders += gameOrder

    /**
     * Game оповещает, что игрок вышел из игры
     */
    case Leaved(externalAccountId) ⇒
      playerToGameInfo -= externalAccountId

    /**
     * Game оповещает, что игра закончена - останавливаем актор игры
     */
    case GameOver ⇒
      sender ! StopGame

    /**
     * Scheduler говорит, что пора пробовать создавать игры из заявок
     */
    case TryCreateGames ⇒ tryCreateGames()
  }

  /**
   * Создать игры из имеющихся заявок
   * Если заявок две - создаем игру между этими двумя игроками
   * Если заявка одна - создаем игру с ботом
   */
  private def tryCreateGames() = {
    if (gameOrders.size == 2 && friendlyDevices(gameOrders(0).deviceType, gameOrders(1).deviceType)) {
      val order1 = gameOrders(0)
      val order2 = gameOrders(1)
      gameOrders -= order1
      gameOrders -= order2
      createGame(isBigGame(order1.deviceType), List(order1, order2))
    } else if (gameOrders.size == 1) {
      val order = gameOrders(0)
      gameOrders -= order
      createGameWithBot(order)
    }
  }

  private def friendlyDevices(a: DeviceType, b: DeviceType) =
    (a == DeviceType.PHONE && b == DeviceType.PHONE) || (a != DeviceType.PHONE && b != DeviceType.PHONE)

  private def isBigGame(deviceType: DeviceType) = deviceType != DeviceType.PHONE

  private val botIdIterator = new BotIdIterator

  private def createGameWithBot(order: GameOrder) = {
    val externalAccountId = botIdIterator.next
    val bot = context.actorOf(Props(classOf[Bot], externalAccountId), externalAccountId.id)
    val botOrder = new GameOrder(externalAccountId, DeviceType.CANVAS, bot, order.startLocation, order.skills, order.items, isBot = true)
    val orders = List(order, botOrder)
    createGame(isBigGame(order.deviceType), orders)
  }

  private val gameIdIterator = new GameIdIterator

  private def createGame(big: Boolean, orders: Iterable[GameOrder]) = {
    println("create game")
    val playerIdIterator = new PlayerIdIterator

    val players = for (order ← orders) yield {
      val playerId = playerIdIterator.next
      playerId → new Player(playerId, order.externalAccountId, order.startLocation, order.skills, order.items, isBot = order.isBot)
    }

    val game = context.actorOf(Props(classOf[Game], players.toMap, big, gameConfig, self), gameIdIterator.next)

    val ids = orders.map(_.externalAccountId)

    val info = new GameInfo(game, ids)

    for (order ← orders) {
      playerToGameInfo += order.externalAccountId → info
      order.accountRef ! ConnectToGame(game)
    }
  }
}

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

import scala.concurrent.duration._

object MatchMaking {

  class GameOrder(val accountId: AccountId,
                  val deviceType: DeviceType,
                  val startLocation: StartLocation,
                  val skills: Skills,
                  val items: Items,
                  val isBot: Boolean)

  // account -> matchmaking

  case class PlaceGameOrder(gameOrder: GameOrder)

  case class InGame(externalAccountId: AccountId)

  // matchmaking -> account

  case class InGameResponse(gameRef: Option[ActorRef], enterGame: Boolean)

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

  private var gameOrders = List[GameOrder]()

  private var accountIdToGameInfo = Map[AccountId, GameInfo]()

  private var accountIdToAccountRef = Map[AccountId, ActorRef]()

  case object TryCreateGames

  import context.dispatcher

  context.system.scheduler.schedule(0 seconds, interval, self, TryCreateGames)

  def receive = {
    /**
     * Аккаунт спрашивает находится ли он сейчас в игре?
     * В ответ отпарвялем InGameState
     */
    case InGame(accountId) ⇒
      accountIdToAccountRef = accountIdToAccountRef.updated(accountId, sender)

      val gameInfo = accountIdToGameInfo.get(accountId)
      if (gameInfo.isEmpty)
        sender ! InGameResponse(None, enterGame = gameOrders.exists(gameOrder ⇒ gameOrder.accountId == accountId))
      else
        sender ! InGameResponse(Some(gameInfo.get.gameRef), enterGame = false)

    /**
     * Аккаунт присылает заявку на игру
     */
    case PlaceGameOrder(gameOrder) ⇒
      assert(accountIdToGameInfo.get(gameOrder.accountId).isEmpty)
      println("place game order")
      accountIdToAccountRef = accountIdToAccountRef.updated(gameOrder.accountId, sender)
      gameOrders = gameOrders :+ gameOrder

    /**
     * Game оповещает, что игрок вышел из игры
     */
    case Leaved(accountId) ⇒
      accountIdToGameInfo = accountIdToGameInfo - accountId

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
   * Дабы игра не была создана раньше чем interval (Нужно для тестов)
   */
  private var currentGameOrders = List[GameOrder]()

  /**
   * Создать игры из имеющихся заявок
   * Если заявок две - создаем игру между этими двумя игроками
   * Если заявка одна - создаем игру с ботом
   */
  private def tryCreateGames() = {
    if (currentGameOrders.size == 2 && friendlyDevices(currentGameOrders(0).deviceType, currentGameOrders(1).deviceType)) {
      val order1 = currentGameOrders(0)
      val order2 = currentGameOrders(1)
      gameOrders = gameOrders.filter(_ != order1)
      gameOrders = gameOrders.filter(_ != order2)

      createGame(isBigGame(order1.deviceType), List(order1, order2))
    } else if (currentGameOrders.size == 1) {
      val order = currentGameOrders(0)
      gameOrders = gameOrders.filter(_ != order)
      createGameWithBot(order)
    }

    currentGameOrders = gameOrders
  }

  private def friendlyDevices(a: DeviceType, b: DeviceType) =
    (a == DeviceType.PHONE && b == DeviceType.PHONE) || (a != DeviceType.PHONE && b != DeviceType.PHONE)

  private def isBigGame(deviceType: DeviceType) = deviceType != DeviceType.PHONE

  private val botIdIterator = new BotIdIterator

  private def createGameWithBot(order: GameOrder) = {
    val bigGame= isBigGame(order.deviceType)

    val botsCount = if(bigGame) 3 else 1

    var orders = List(order)

    for(i ← 0 until botsCount) {
      val externalAccountId = botIdIterator.next
      val bot = context.actorOf(Props(classOf[Bot], externalAccountId), externalAccountId.id)
      accountIdToAccountRef = accountIdToAccountRef.updated(externalAccountId, bot)
      val botOrder = new GameOrder(externalAccountId, DeviceType.CANVAS, order.startLocation, order.skills, order.items, isBot = true)
      orders = orders :+ botOrder
    }

    createGame(bigGame, orders)
  }

  private val gameIdIterator = new GameIdIterator

  private def createGame(big: Boolean, orders: Iterable[GameOrder]) = {
    println("create game")
    val playerIdIterator = new PlayerIdIterator

    val players = for (order ← orders) yield {
      val playerId = playerIdIterator.next
      playerId → new Player(playerId, order.accountId, order.startLocation, order.skills, order.items, isBot = order.isBot)
    }

    val game = context.actorOf(Props(classOf[Game], players.toMap, big, gameConfig, self), gameIdIterator.next)

    val ids = orders.map(_.accountId)

    val info = new GameInfo(game, ids)

    for (order ← orders) {
      accountIdToGameInfo = accountIdToGameInfo + (order.accountId → info)
      accountIdToAccountRef(order.accountId) ! ConnectToGame(game)
    }
  }
}

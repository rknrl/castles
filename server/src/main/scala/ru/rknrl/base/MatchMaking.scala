package ru.rknrl.base

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorRef, OneForOneStrategy}
import ru.rknrl.base.MatchMaking._
import ru.rknrl.base.account.LeaveGame
import ru.rknrl.base.game.Game.StopGame
import ru.rknrl.castles.account.objects.{Items, Skills, StartLocation}
import ru.rknrl.castles.game.GameConfig
import ru.rknrl.castles.game.objects.players.PlayerId
import ru.rknrl.dto.CommonDTO.{AccountType, DeviceType, ItemType, UserInfoDTO}
import ru.rknrl.utils.IdIterator

import scala.concurrent.duration._

class BotIdIterator extends IdIterator {
  def next = new AccountId(AccountType.DEV, "bot" + nextInt)
}

class GameIdIterator extends IdIterator {
  def next = "game" + nextInt
}

class PlayerIdIterator extends IdIterator {
  def next = new PlayerId(nextInt)
}

object MatchMaking {

  class GameOrder(val accountId: AccountId,
                  val deviceType: DeviceType,
                  val userInfo: UserInfoDTO,
                  val startLocation: StartLocation,
                  val skills: Skills,
                  val items: Items,
                  val rating: Double,
                  val gamesCount: Int,
                  val isBot: Boolean)

  // account -> matchmaking

  case class PlaceGameOrder(gameOrder: GameOrder)

  case class InGame(externalAccountId: AccountId)

  // matchmaking -> account

  case class InGameResponse(gameRef: Option[ActorRef], enterGame: Boolean)

  case class ConnectToGame(game: ActorRef)

  // game -> matchmaking

  case class PlayerLeaveGame(externalAccountId: AccountId, place: Int, reward: Int, usedItems: Map[ItemType, Int])

  case object AllPlayersLeaveGame

  case class TopItem(accountId: AccountId, rating: Double)

}

abstract class MatchMaking(interval: FiniteDuration, var top: List[TopItem], gameConfig: GameConfig) extends Actor {
  /** Если у бота случается ошибка - стопаем его
    * Если в игре случается ошибка, посылаем всем не вышедшим игрокам LeaveGame и стопаем актор игры
    */
  override def supervisorStrategy = OneForOneStrategy() {
    case _: Exception ⇒
      if (gameRefToGameInfo.contains(sender)) {
        val gameInfo = gameRefToGameInfo(sender)
        for (order ← gameInfo.orders;
             accountId = order.accountId
             if accountIdToGameInfo.contains(accountId) && accountIdToGameInfo(accountId) == gameInfo) {
          onAccountLeaveGame(accountId, place = gameInfo.orders.size, reward = 0, usedItems = Map.empty)
        }
        onGameOver(sender)
        Stop
      } else
        Stop // stop bot
  }

  class GameInfo(val gameRef: ActorRef,
                 val orders: Iterable[GameOrder])

  private var gameOrders = List[GameOrder]()

  private var accountIdToGameInfo = Map[AccountId, GameInfo]()

  private var gameRefToGameInfo = Map[ActorRef, GameInfo]()

  private var accountIdToAccountRef = Map[AccountId, ActorRef]()

  case object TryCreateGames

  import context.dispatcher

  context.system.scheduler.schedule(0 seconds, interval, self, TryCreateGames)

  /** Создать игры из имеющихся заявок
    */
  protected def tryCreateGames(gameOrders: List[GameOrder]): Iterable[GameInfo]

  def receive = {
    /** Аккаунт спрашивает находится ли он сейчас в игре?
      * В ответ отпарвялем InGameState
      */
    case InGame(accountId) ⇒
      accountIdToAccountRef = accountIdToAccountRef.updated(accountId, sender)

      val gameInfo = accountIdToGameInfo.get(accountId)
      if (gameInfo.isEmpty)
        sender ! InGameResponse(None, enterGame = gameOrders.exists(gameOrder ⇒ gameOrder.accountId == accountId))
      else
        sender ! InGameResponse(Some(gameInfo.get.gameRef), enterGame = false)

    /** Аккаунт присылает заявку на игру */
    case PlaceGameOrder(gameOrder) ⇒ placeGameOrder(gameOrder, sender)

    /** Game оповещает, что игрок вышел из игры */
    case PlayerLeaveGame(accountId, place, reward, usedItems) ⇒ onAccountLeaveGame(accountId, place, reward, usedItems)

    /** Game оповещает, что игра закончена - останавливаем актор игры */
    case AllPlayersLeaveGame ⇒
      onGameOver(sender)
      sender ! StopGame

    /** Scheduler говорит, что пора пробовать создавать игры из заявок */
    case TryCreateGames ⇒
      tryCreateGames(gameOrders).map(registerGame)
  }

  private def getSA(big: Boolean, place: Int) =
    if (big)
      place match {
        case 1 ⇒ 1.0
        case 2 ⇒ 0.5
        case 3 ⇒ 0.25
        case 4 ⇒ 0.0
      }
    else
    if (place == 1) 1.0 else 0.0

  /** http://en.wikipedia.org/wiki/Elo_rating_system */
  private def getNewRating(ratingA: Double, ratingB: Double, gamesCountA: Int, sA: Double) = {
    val eA: Double = 1 / (1 + Math.pow(10, (ratingB - ratingA) / 400))

    val k: Double = if (ratingA > 2400) 10 else if (gamesCountA <= 30) 30 else 15

    ratingA + k * (sA - eA)
  }

  protected final def placeGameOrder(gameOrder: GameOrder, accountRef: ActorRef) = {
    assert(accountIdToGameInfo.get(gameOrder.accountId).isEmpty)
    accountIdToAccountRef = accountIdToAccountRef.updated(gameOrder.accountId, accountRef)
    gameOrders = gameOrders :+ gameOrder
  }

  protected final def registerGame(info: GameInfo) = {
    gameRefToGameInfo = gameRefToGameInfo + (info.gameRef → info)

    for (order ← info.orders) {
      gameOrders = gameOrders.filter(_ != order)
      accountIdToGameInfo = accountIdToGameInfo + (order.accountId → info)
      accountIdToAccountRef(order.accountId) ! ConnectToGame(info.gameRef)
    }
  }

  private def onAccountLeaveGame(accountId: AccountId, place: Int, reward: Int, usedItems: Map[ItemType, Int]) = {
    accountIdToGameInfo = accountIdToGameInfo - accountId

    val gameInfo = gameRefToGameInfo(sender)
    val orders = gameInfo.orders
    val order = orders.find(_.accountId == accountId).get
    val big = orders.size == 4

    val averageEnemyRating = orders.filter(_ != order).map(_.rating).sum / (orders.size - 1)

    val sA = getSA(big, place)
    val newRating = getNewRating(order.rating, averageEnemyRating, order.gamesCount, sA)

    top = insert(top, TopItem(accountId, newRating))

    accountIdToAccountRef(accountId) ! LeaveGame(usedItems, reward, newRating)
  }

  private def insert(list: List[TopItem], item: TopItem) =
    (top.filter(_.accountId != item.accountId) :+ item).sortBy(_.rating).take(5)

  private def onGameOver(gameRef: ActorRef) =
    gameRefToGameInfo = gameRefToGameInfo - gameRef

}

package ru.rknrl.castles

import akka.actor.SupervisorStrategy.Stop
import akka.actor._
import ru.rknrl.castles.MatchMaking._
import ru.rknrl.castles.account.Account.{DuplicateAccount, LeaveGame}
import ru.rknrl.castles.account.state._
import ru.rknrl.castles.bot.Bot
import ru.rknrl.castles.game.Game.StopGame
import ru.rknrl.castles.game._
import ru.rknrl.castles.game.state.players.{Player, PlayerId}
import ru.rknrl.castles.payments.PaymentsServer.{AccountNotOnline, AddProduct}
import ru.rknrl.dto.AccountDTO.AccountStateDTO
import ru.rknrl.dto.AuthDTO.TopUserInfoDTO
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
                  val slots: Slots,
                  val skills: Skills,
                  val items: Items,
                  val rating: Double,
                  val gamesCount: Int,
                  val isBot: Boolean)

  // admin -> matchmakin

  case class AdminSetAccountState(accountId: AccountId, accountState: AccountStateDTO)

  // account -> matchmaking

  case class PlaceGameOrder(gameOrder: GameOrder)

  case class InGame(externalAccountId: AccountId)

  // matchmaking -> account

  case class InGameResponse(gameRef: Option[ActorRef], searchOpponents: Boolean, top: Iterable[TopUserInfoDTO])

  case class ConnectToGame(game: ActorRef)

  // game -> matchmaking

  case class PlayerLeaveGame(externalAccountId: AccountId, place: Int, reward: Int, usedItems: Map[ItemType, Int], userInfo: UserInfoDTO)

  case object AllPlayersLeaveGame

  case class TopItem(accountId: AccountId, rating: Double, info: UserInfoDTO)

}

class MatchMaking(interval: FiniteDuration, var top: List[TopItem], gameConfig: GameConfig) extends Actor {
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
          onAccountLeaveGame(accountId, place = gameInfo.orders.size, reward = 0, usedItems = Map.empty, gameInfo.orders.find(_.accountId == accountId).get.userInfo)
        }
        onGameOver(sender)
        Stop
      } else
        Stop // stop bot
  }

  class GameInfo(val gameRef: ActorRef,
                 val orders: Iterable[GameOrder]) {
    def big = orders.size == 4
  }

  var gameOrders = List[GameOrder]()

  var accountIdToGameInfo = Map[AccountId, GameInfo]()

  var gameRefToGameInfo = Map[ActorRef, GameInfo]()

  var accountIdToAccountRef = Map[AccountId, ActorRef]()

  case object TryCreateGames

  import context.dispatcher

  context.system.scheduler.schedule(0 seconds, interval, self, TryCreateGames)

  /** Создать игры из имеющихся заявок
    */
  def tryCreateGames(gameOrders: List[GameOrder]) = {
    val (smallGameOrders, bigGameOrders) = gameOrders.span(_.deviceType == DeviceType.PHONE)

    createGames(big = false, playersCount = 2, smallGameOrders) ++
      createGames(big = true, playersCount = 4, bigGameOrders)
  }

  def createGames(big: Boolean, playersCount: Int, orders: List[GameOrder]) = {
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

  val botIdIterator = new BotIdIterator

  def createGameWithBot(big: Boolean, playerCount: Int, orders: List[GameOrder]) = {
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

  def botItems(playerItems: Items) =
    new Items(playerItems.items.map {
      case (itemType, item) ⇒ (itemType, new Item(itemType, item.count * 2))
    })

  def botUserInfo(accountId: AccountId, number: Int) =
    UserInfoDTO.newBuilder()
      .setAccountId(accountId.dto)
      .setFirstName("Бот")
      .setLastName(number.toString)
      .setPhoto96("1")
      .setPhoto256("1")
      .build()

  val gameIdIterator = new GameIdIterator

  def createGame(big: Boolean, orders: Iterable[GameOrder]) = {
    val playerIdIterator = new PlayerIdIterator

    val players = for (order ← orders) yield {
      val playerId = playerIdIterator.next
      playerId → new Player(playerId, order.accountId, order.userInfo, order.slots, order.skills, order.items, isBot = order.isBot)
    }

    val game = context.actorOf(Props(classOf[Game], players.toMap, big, gameConfig, self), gameIdIterator.next)

    new GameInfo(game, orders)
  }

  def receive = {
    /** from Admin */
    case msg@AdminSetAccountState(accountId, _) ⇒
      if (accountIdToAccountRef.contains(accountId))
        accountIdToAccountRef(accountId) forward msg

    /** from PaymentServer */
    case msg@AddProduct(accountId, _, _, _) ⇒
      if (accountIdToAccountRef.contains(accountId))
        accountIdToAccountRef(accountId) forward msg
      else
        sender ! AccountNotOnline

    /** Аккаунт спрашивает находится ли он сейчас в игре?
      * В ответ отправляем InGameResponse
      */
    case InGame(accountId) ⇒
      if (accountIdToAccountRef.contains(accountId)) {
        val oldAccountRef = accountIdToAccountRef(accountId)
        oldAccountRef ! DuplicateAccount
      }
      accountIdToAccountRef = accountIdToAccountRef.updated(accountId, sender)

      val gameInfo = accountIdToGameInfo.get(accountId)
      if (gameInfo.isEmpty)
        sender ! InGameResponse(None, searchOpponents = gameOrders.exists(gameOrder ⇒ gameOrder.accountId == accountId), topDto)
      else
        sender ! InGameResponse(Some(gameInfo.get.gameRef), searchOpponents = false, topDto)

    /** Аккаунт присылает заявку на игру */
    case PlaceGameOrder(gameOrder) ⇒ placeGameOrder(gameOrder, sender)

    /** Game оповещает, что игрок вышел из игры */
    case PlayerLeaveGame(accountId, place, reward, usedItems, userInfo) ⇒ onAccountLeaveGame(accountId, place, reward, usedItems, userInfo)

    /** Game оповещает, что игра закончена - останавливаем актор игры */
    case AllPlayersLeaveGame ⇒
      onGameOver(sender)
      sender ! StopGame

    /** Scheduler говорит, что пора пробовать создавать игры из заявок */
    case TryCreateGames ⇒
      tryCreateGames(gameOrders).map(registerGame)
  }

  def getSA(big: Boolean, place: Int) =
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
  def getNewRating(ratingA: Double, ratingB: Double, gamesCountA: Int, sA: Double) = {
    val eA: Double = 1 / (1 + Math.pow(10, (ratingB - ratingA) / 400))

    val k: Double = if (ratingA > 2400) 10 else if (gamesCountA <= 30) 30 else 15

    ratingA + k * (sA - eA)
  }

  def placeGameOrder(gameOrder: GameOrder, accountRef: ActorRef) = {
    assert(accountIdToGameInfo.get(gameOrder.accountId).isEmpty)
    accountIdToAccountRef = accountIdToAccountRef.updated(gameOrder.accountId, accountRef)
    gameOrders = gameOrders :+ gameOrder
  }

  def registerGame(info: GameInfo) = {
    gameRefToGameInfo = gameRefToGameInfo + (info.gameRef → info)

    for (order ← info.orders) {
      gameOrders = gameOrders.filter(_ != order)
      accountIdToGameInfo = accountIdToGameInfo + (order.accountId → info)
      accountIdToAccountRef(order.accountId) ! ConnectToGame(info.gameRef)
    }
  }

  def onAccountLeaveGame(accountId: AccountId, place: Int, reward: Int, usedItems: Map[ItemType, Int], userInfo: UserInfoDTO) = {
    accountIdToGameInfo = accountIdToGameInfo - accountId

    val gameInfo = gameRefToGameInfo(sender)
    val orders = gameInfo.orders
    val order = orders.find(_.accountId == accountId).get

    val averageEnemyRating = orders.filter(_ != order).map(_.rating).sum / (orders.size - 1)

    val sA = getSA(gameInfo.big, place)
    val newRating = getNewRating(order.rating, averageEnemyRating, order.gamesCount, sA)

    top = insert(top, TopItem(accountId, newRating, userInfo))

    accountIdToAccountRef(accountId) ! LeaveGame(usedItems, reward, newRating)
  }

  def insert(list: List[TopItem], item: TopItem) =
    (top.filter(_.accountId != item.accountId) :+ item).sortBy(_.rating).take(5)

  def topDto =
    for (i ← 0 until top.size)
    yield TopUserInfoDTO.newBuilder()
      .setPlace(i + 1)
      .setInfo(top(i).info)
      .build

  def onGameOver(gameRef: ActorRef) =
    gameRefToGameInfo = gameRefToGameInfo - gameRef

}

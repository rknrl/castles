//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles

import akka.actor.SupervisorStrategy.Stop
import akka.actor._
import org.slf4j.LoggerFactory
import ru.rknrl.castles.MatchMaking._
import ru.rknrl.castles.account.Account.{DuplicateAccount, LeaveGame}
import ru.rknrl.castles.account.AccountState.{Items, Slots}
import ru.rknrl.castles.bot.{Bot, TutorBot}
import ru.rknrl.castles.database.Database
import ru.rknrl.castles.database.Statistics.updateStatistics
import ru.rknrl.castles.game._
import ru.rknrl.castles.game.state.{Player, Stat}
import ru.rknrl.castles.rmi.B2C.ServerHealth
import ru.rknrl.castles.rmi.C2B.GetServerHealth
import ru.rknrl.dto._
import ru.rknrl.utils.IdIterator
import ru.rknrl.{Assertion, Logged, Slf4j}

import scala.concurrent.duration._

class BotIdIterator extends IdIterator {
  def next = AccountId(AccountType.DEV, "bot" + nextInt)
}

class GameIdIterator extends IdIterator {
  def next = "game" + nextInt
}

class PlayerIdIterator extends IdIterator {
  def next = PlayerId(nextInt)
}

object MatchMaking {

  case class GameOrder(accountId: AccountId,
                       deviceType: DeviceType,
                       userInfo: UserInfoDTO,
                       slots: Slots,
                       stat: Stat,
                       items: Items,
                       rating: Double,
                       gamesCount: Int,
                       isBot: Boolean,
                       isTutor: Boolean)

  // admin -> matchmaking

  case class AdminSetAccountState(accountId: AccountId, accountState: AccountStateDTO)

  // account -> matchmaking

  case class InGame(externalAccountId: AccountId)

  case class Offline(accountId: AccountId)

  // matchmaking -> account

  case class InGameResponse(gameRef: Option[ActorRef], searchOpponents: Boolean, top: Iterable[TopUserInfoDTO])

  case class ConnectToGame(game: ActorRef)

  // game -> matchmaking

  case class PlayerLeaveGame(accountId: AccountId, place: Int, reward: Int, usedItems: Map[ItemType, Int], userInfo: UserInfoDTO)

  case object AllPlayersLeaveGame

  case class TopItem(accountId: AccountId, rating: Double, info: UserInfoDTO)

}

class MatchMaking(interval: FiniteDuration,
                  database: ActorRef,
                  bugs: ActorRef,
                  var top: List[TopItem],
                  config: Config,
                  gameMaps: GameMaps) extends Actor {
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
      }

      Stop
  }

  val log = new Slf4j(LoggerFactory.getLogger(getClass))

  def logged(r: Receive) = new Logged(r, log, None, None, {
    case TryCreateGames ⇒ false
    case RegisterHealth ⇒ false
    case _ ⇒ true
  })

  class GameInfo(val gameRef: ActorRef,
                 val orders: Iterable[GameOrder],
                 val isTutor: Boolean) {
    def big = orders.size == 4

    def order(accountId: AccountId) = orders.find(_.accountId == accountId).get
  }

  var gameOrders = List[GameOrder]()

  var accountIdToGameInfo = Map[AccountId, GameInfo]()

  var gameRefToGameInfo = Map[ActorRef, GameInfo]()

  var accountIdToAccountRef = Map[AccountId, ActorRef]()

  case object TryCreateGames

  case object RegisterHealth

  import context.dispatcher

  context.system.scheduler.schedule(0 seconds, interval, self, TryCreateGames)

  context.system.scheduler.schedule(0 seconds, 1 minute, self, RegisterHealth)

  /** Создать игры из имеющихся заявок */
  def tryCreateGames(gameOrders: List[GameOrder]) = {
    val (smallGameOrders, bigGameOrders) = gameOrders.span(_.deviceType == DeviceType.PHONE)

    createGames(big = false, playersCount = 2, smallGameOrders) ++
      createGames(big = true, playersCount = 4, bigGameOrders)
  }

  def createGames(big: Boolean, playersCount: Int, orders: List[GameOrder]) = {
    val (tutorOrders, notTutorOrders) = orders.span(_.isTutor)

    var createdGames = tutorOrders.map(order ⇒ createGameWithBot(big, playersCount, List(order), isTutor = true))

    var sorted = notTutorOrders.sortBy(_.rating)(Ordering.Double.reverse)

    while (sorted.size > playersCount) {
      createdGames = createdGames :+ createGame(big, sorted.take(playersCount), isTutor = false)
      sorted = sorted.drop(playersCount)
    }

    if (sorted.size > 0)
      createdGames = createdGames :+ createGameWithBot(big, playersCount, sorted, isTutor = false)

    createdGames
  }

  val botIdIterator = new BotIdIterator
  val tutorBotStat = new Stat(attack = 0.3, defence = 0.3, speed = 1)

  def createGameWithBot(big: Boolean, playerCount: Int, orders: List[GameOrder], isTutor: Boolean) = {
    val botsCount = if (big) playerCount - orders.size else playerCount - orders.size
    Assertion.check(botsCount >= 1, botsCount)

    val order = orders.head
    var result = orders

    for (i ← 0 until botsCount) {
      val accountId = botIdIterator.next
      val botClass = if (isTutor) classOf[TutorBot] else classOf[Bot]
      val bot = context.actorOf(Props(botClass, accountId, config.game, bugs), accountId.id)
      val botStat = if (isTutor) tutorBotStat else order.stat
      val botOrder = new GameOrder(accountId, order.deviceType, botUserInfo(accountId, i), order.slots, botStat, botItems(order.items), order.rating, order.gamesCount, isBot = true, isTutor)
      result = result :+ botOrder
      placeGameOrder(botOrder, bot)
    }

    createGame(big, result, isTutor)
  }

  def botItems(playerItems: Items) =
    playerItems.mapValues(count ⇒ count * 2)

  def botUserInfo(accountId: AccountId, number: Int) =
    number match {
      case 0 ⇒
        UserInfoDTO(
          accountId,
          Some("Sasha"),
          Some("Serova"),
          Some("http://" + config.staticHost + "/avatars/Sasha96.png"),
          Some("http://" + config.staticHost + "/avatars/Sasha256.png")
        )
      case 1 ⇒
        UserInfoDTO(
          accountId,
          Some("Napoleon"),
          Some("1769"),
          Some("http://" + config.staticHost + "/avatars/Napoleon96.png"),
          Some("http://" + config.staticHost + "/avatars/Napoleon256.png")
        )
      case 2 ⇒
        UserInfoDTO(
          accountId,
          Some("Виктория"),
          Some("Викторовна"),
          Some("http://" + config.staticHost + "/avatars/Babka96.png"),
          Some("http://" + config.staticHost + "/avatars/Babka256.png")
        )
    }

  val gameIdIterator = new GameIdIterator

  def createGame(big: Boolean, orders: Iterable[GameOrder], isTutor: Boolean) = {
    log.debug("createGame")
    val playerIdIterator = new PlayerIdIterator

    val players = for (order ← orders) yield {
      val playerId = playerIdIterator.next
      playerId → new Player(playerId, order.accountId, order.userInfo, order.slots, order.stat, order.items, isBot = order.isBot)
    }

    val gameConfig = if (isTutor) config.game.tutorConfig else config.game

    val gameMap = if (isTutor) gameMaps.tutor(big) else gameMaps.random(big)

    val game = context.actorOf(Props(classOf[Game], players.toMap, big, isTutor, config.isDev, gameConfig, gameMap, self, bugs), gameIdIterator.next)

    if (!isTutor) {
      if (orders.count(_.isBot) == orders.size - 1) {
        if (orders.size == 4)
          database ! updateStatistics(StatAction.START_GAME_4_WITH_BOTS)
        else
          database ! updateStatistics(StatAction.START_GAME_2_WITH_BOTS)
      } else {
        if (orders.size == 4)
          database ! updateStatistics(StatAction.START_GAME_4_WITH_PLAYERS)
        else
          database ! updateStatistics(StatAction.START_GAME_2_WITH_PLAYERS)
      }
    }

    new GameInfo(game, orders, isTutor)
  }

  val healthStartTime = System.currentTimeMillis()
  var health = List.empty[ServerHealthItemDTO]

  def receive = logged({
    case RegisterHealth ⇒
      health = health :+ ServerHealthItemDTO(
        online = accountIdToAccountRef.size,
        games = gameRefToGameInfo.size,
        totalMem = (Runtime.getRuntime.totalMemory() / 1024 / 1024).toInt
      )

    /** from Admin or PaymentTransaction */
    case msg@AdminSetAccountState(accountId, _) ⇒
      if (accountIdToAccountRef.contains(accountId))
        accountIdToAccountRef(accountId) forward msg

    /** from Admin */
    case GetServerHealth ⇒
      sender ! ServerHealth(
        ServerHealthDTO(
          startTime = healthStartTime,
          items = health
        )
      )

    /** from Admin */
    case Database.AccountDeleted(accountId) ⇒
      if (accountIdToAccountRef.contains(accountId))
        accountIdToAccountRef(accountId) ! DuplicateAccount

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

    /** Аккаунт отсоединился */
    case Offline(accountId) ⇒
      if (accountIdToAccountRef.contains(accountId) && accountIdToAccountRef(accountId) == sender) {
        accountIdToAccountRef = accountIdToAccountRef - accountId
        val gameInfo = accountIdToGameInfo.get(accountId)
        if (gameInfo.isDefined) {

          // Если это тутор и игрок отвалился, то убиваем игру.
          // При перезаходе игрока будет создана новая игра (Иначе новичок не поймет, что произошло)
          if (gameInfo.get.isTutor && !gameInfo.get.order(accountId).isBot) {
            accountIdToGameInfo = accountIdToGameInfo - accountId
            onGameOver(gameInfo.get.gameRef)
            context stop gameInfo.get.gameRef
          } else {
            gameInfo.get.gameRef ! Offline(accountId)
          }
        }
      }

    /** Аккаунт присылает заявку на игру */
    case gameOrder: GameOrder ⇒
      placeGameOrder(gameOrder, sender)

    /** Game оповещает, что игрок вышел из игры */
    case PlayerLeaveGame(accountId, place, reward, usedItems, userInfo) ⇒
      onAccountLeaveGame(accountId, place, reward, usedItems, userInfo)

    /** Game оповещает, что игра закончена - останавливаем актор игры */
    case AllPlayersLeaveGame ⇒
      onGameOver(sender)
      context stop sender

    /** Scheduler говорит, что пора пробовать создавать игры из заявок */
    case TryCreateGames ⇒
      tryCreateGames(gameOrders).map(registerGame)
  })

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
    Assertion.check(accountIdToGameInfo.get(gameOrder.accountId).isEmpty)
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

    accountIdToAccountRef(accountId) ! LeaveGame(usedItems, reward, newRating, topDto) // todo - если он ушел в оффлайн, ничо не сохранится

    val gameWithBots = orders.count(_.isBot) == orders.size - 1
    if (gameWithBots && !order.isBot) {
      if (orders.size == 4) {
        if (place == 1) {
          if (gameInfo.isTutor)
            database ! updateStatistics(StatAction.TUTOR_4_WIN)
          else
            database ! updateStatistics(StatAction.WIN_4_BOTS)
        } else {
          if (gameInfo.isTutor)
            database ! updateStatistics(StatAction.TUTOR_4_LOSE)
          else
            database ! updateStatistics(StatAction.LOSE_4_BOTS)
        }
      } else if (orders.size == 2) {
        if (place == 1) {
          if (gameInfo.isTutor)
            database ! updateStatistics(StatAction.TUTOR_2_WIN)
          else
            database ! updateStatistics(StatAction.WIN_2_BOTS)
        } else {
          if (gameInfo.isTutor)
            database ! updateStatistics(StatAction.TUTOR_2_LOSE)
          else
            database ! updateStatistics(StatAction.LOSE_2_BOTS)
        }
      }
    }
  }

  def insert(list: List[TopItem], item: TopItem) =
    (top.filter(_.accountId != item.accountId) :+ item).sortBy(_.rating).take(5)

  def topDto =
    for (i ← 0 until top.size)
      yield TopUserInfoDTO(
        place = i + 1,
        info = top(i).info
      )

  def onGameOver(gameRef: ActorRef) = {
    val gameInfo = gameRefToGameInfo(gameRef)
    for (order ← gameInfo.orders if order.isBot) {
      val bot = accountIdToAccountRef(order.accountId)
      accountIdToAccountRef = accountIdToAccountRef - order.accountId
      context stop bot
    }
    gameRefToGameInfo = gameRefToGameInfo - gameRef
  }
}

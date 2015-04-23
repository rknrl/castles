//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.matchmaking

import akka.actor.SupervisorStrategy.Stop
import akka.actor._
import org.slf4j.LoggerFactory
import ru.rknrl.castles.Config
import ru.rknrl.castles.account.AccountState
import ru.rknrl.castles.account.AccountState.Items
import ru.rknrl.castles.bot.{Bot, TutorBot}
import ru.rknrl.castles.database.{Database, Statistics}
import ru.rknrl.castles.game._
import ru.rknrl.castles.game.init.{GameMaps, GameStateInit}
import ru.rknrl.castles.game.state.Player
import ru.rknrl.castles.matchmaking.NewMatchmaking._
import ru.rknrl.core.Stat
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

class MatchMaking(interval: FiniteDuration,
                  database: ActorRef,
                  bugs: ActorRef,
                  var top: Top,
                  config: Config,
                  gameMaps: GameMaps) extends Actor {
  /** Если у бота случается ошибка - стопаем его
    * Если в игре случается ошибка, посылаем всем не вышедшим игрокам LeaveGame и стопаем актор игры
    */
  override def supervisorStrategy = OneForOneStrategy() {
    case e: Exception ⇒
      if (gameRefToGameInfo.contains(sender)) {
        val gameInfo = gameRefToGameInfo(sender)
        for (order ← gameInfo.orders;
             accountId = order.accountId
             if accountIdToGameInfo.contains(accountId) && accountIdToGameInfo(accountId) == gameInfo) {
          onAccountLeaveGame(accountId, place = gameInfo.orders.size, reward = 0, usedItems = Map.empty)
        }
        onGameOver(sender)
      }

      if (config.isDev) throw new Error(e)

      Stop
  }

  val log = new Slf4j(LoggerFactory.getLogger(getClass))

  def logged(r: Receive) = new Logged(r, log, None, None, {
    case TryCreateGames ⇒ false
    case _ ⇒ true
  })

  var gameOrders = List.empty[GameOrder]

  var accountIdToGameInfo = Map.empty[AccountId, GameInfo]

  var gameRefToGameInfo = Map.empty[ActorRef, GameInfo]

  var accountIdToAccountRef = Map.empty[AccountId, ActorRef]

  import context.dispatcher

  context.system.scheduler.schedule(0 seconds, interval, self, TryCreateGames)

  /** Создать игры из имеющихся заявок */
  def tryCreateGames(gameOrders: List[GameOrder]) = {
    val (smallGameOrders, bigGameOrders) = gameOrders.span(_.deviceType == DeviceType.PHONE)

    createGames(big = false, playersCount = 2, smallGameOrders) ++
      createGames(big = true, playersCount = 4, bigGameOrders)
  }

  def createGames(big: Boolean, playersCount: Int, orders: List[GameOrder]) = {
    val (tutorOrders, notTutorOrders) = orders.span(_.accountState.gamesCount == 0)

    var createdGames = tutorOrders.map(order ⇒ createGameWithBot(big, playersCount, List(order), isTutor = true))

    var sorted = notTutorOrders.sortBy(_.accountState.rating)(Ordering.Double.reverse)

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

      val botAccountState = new AccountState(
        slots = order.accountState.slots,
        skills = order.accountState.skills,
        items = botItems(order.accountState.items),
        gold = order.accountState.gold,
        rating = order.accountState.rating,
        gamesCount = order.accountState.gamesCount
      )
      val botOrder = new GameOrder(accountId, order.deviceType, config.botUserInfo(accountId, i), botAccountState, isBot = true)
      result = result :+ botOrder
      placeGameOrder(botOrder, bot)
    }

    createGame(big, result, isTutor)
  }

  def botItems(playerItems: Items) =
    playerItems.mapValues(count ⇒ count * 2)

  val gameIdIterator = new GameIdIterator

  def createGame(big: Boolean, orders: Iterable[GameOrder], isTutor: Boolean) = {
    log.debug("createGame")
    val playerIdIterator = new PlayerIdIterator

    val players = for (order ← orders) yield {
      val playerId = playerIdIterator.next

      val realStat = config.account.skillsToStat(order.accountState.skills)
      val stat = if (order.isBot) {
        if (isTutor) tutorBotStat else realStat
      } else {
        if (isTutor)
          Stat(
            attack = realStat.attack * 3,
            defence = realStat.defence * 3,
            speed = realStat.speed
          )
        else
          realStat
      }

      new Player(playerId, order.accountId, order.userInfo, order.accountState.slots, stat, order.accountState.items, isBot = order.isBot)
    }

    val gameMap = if (isTutor) gameMaps.tutor(big) else gameMaps.random(big)

    val gameState = GameStateInit.init(
      time = System.currentTimeMillis(),
      players = players.toList,
      big = big,
      isTutor = isTutor,
      config = config.game,
      gameMap = gameMap
    )
    val game = context.actorOf(Props(classOf[Game], gameState, config.isDev, classOf[GameScheduler], self, bugs), gameIdIterator.next)

    if (!isTutor) Statistics.sendCreateGameStatistics(orders, database)

    new GameInfo(game, orders, isTutor)
  }

  def receive = logged({
    /** from Admin or PaymentTransaction */
    case msg@SetAccountState(accountId, _) ⇒
      if (accountIdToAccountRef.contains(accountId))
        accountIdToAccountRef(accountId) forward msg

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
        sender ! InGameResponse(None, searchOpponents = gameOrders.exists(gameOrder ⇒ gameOrder.accountId == accountId), top.dto)
      else
        sender ! InGameResponse(Some(gameInfo.get.gameRef), searchOpponents = false, top.dto)

    /** Аккаунт отсоединился */
    case Offline(accountId, client) ⇒
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
            gameInfo.get.gameRef ! Offline(accountId, client)
          }
        }
      }

    /** Аккаунт присылает заявку на игру */
    case gameOrder: GameOrder ⇒
      placeGameOrder(gameOrder, sender)

    /** Game оповещает, что игрок вышел из игры */
    case PlayerLeaveGame(accountId, place, reward, usedItems) ⇒
      onAccountLeaveGame(accountId, place, reward, usedItems)

    /** Game оповещает, что игра закончена - останавливаем актор игры */
    case AllPlayersLeaveGame(gameRef) ⇒
      onGameOver(sender)
      context stop sender

    /** Scheduler говорит, что пора пробовать создавать игры из заявок */
    case TryCreateGames ⇒
      tryCreateGames(gameOrders).foreach(registerGame)
  })

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

  def onAccountLeaveGame(accountId: AccountId, place: Int, reward: Int, usedItems: Map[ItemType, Int]) = {
    accountIdToGameInfo = accountIdToGameInfo - accountId

    val gameInfo = gameRefToGameInfo(sender)
    val orders = gameInfo.orders
    val order = gameInfo.order(accountId)
    val newRating = ELO.newRating(gameInfo.orders, order, place)
    top = top.insert(TopUser(accountId, newRating, order.userInfo))

    context.actorOf(Props(classOf[Patcher], accountId, reward, usedItems, newRating, self, database))

    accountIdToAccountRef(accountId) ! AccountLeaveGame(top.dto)

    Statistics.sendLeaveGameStatistics(place, gameInfo.isTutor, orders, order, database)
  }

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

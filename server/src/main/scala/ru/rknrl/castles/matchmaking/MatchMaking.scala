//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.matchmaking

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorRef, OneForOneStrategy, Props}
import protos._
import ru.rknrl.castles.Config
import ru.rknrl.castles.storage.Storage.AccountStateUpdated
import ru.rknrl.castles.storage.Statistics.{createGameStatistics, leaveGameStatistics}
import ru.rknrl.castles.matchmaking.MatchMaking._
import ru.rknrl.castles.matchmaking.Matcher.matchOrders
import ru.rknrl.core.Graphite.Health
import ru.rknrl.logging.ShortActorLogging

import scala.concurrent.duration.{FiniteDuration, _}

object MatchMaking {

  case class GameOrder(accountId: AccountId,
                       deviceType: DeviceType,
                       userInfo: UserInfo,
                       accountState: AccountState,
                       rating: Double,
                       isBot: Boolean)

  case class GameInfo(gameRef: ActorRef,
                      orders: Iterable[GameOrder],
                      isTutor: Boolean) {
    def order(accountId: AccountId) = orders.find(_.accountId == accountId).get
  }

  case class Online(accountId: AccountId)

  case class Offline(accountId: AccountId, client: ActorRef)

  case class ConnectToGame(gameRef: ActorRef)

  case class InGame(accountId: AccountId)

  case class InGameResponse(gameRef: Option[ActorRef], searchOpponents: Boolean)

  case class PlayerLeaveGame(accountId: AccountId, place: Int, reward: Int, usedItems: Map[ItemType, Int])

  case class AllPlayersLeaveGame(gameRef: ActorRef)

  case object DuplicateAccount

  case object TryCreateGames

  case object RegisterHealth

  case object AccountLeaveGame

  def props(gameCreator: GameCreator,
            gameFactory: IGameFactory,
            interval: FiniteDuration,
            config: Config,
            storage: ActorRef,
            graphite: ActorRef) =
    Props(classOf[MatchMaking], gameCreator, gameFactory, interval, config, storage, graphite)
}

class MatchMaking(gameCreator: GameCreator,
                  gameFactory: IGameFactory,
                  interval: FiniteDuration,
                  config: Config,
                  storage: ActorRef,
                  graphite: ActorRef) extends Actor with ShortActorLogging {

  override def supervisorStrategy = OneForOneStrategy() {
    case e: Exception ⇒
      val gameEntry = accountIdToGameInfo.find { case (accountId, gameInfo) ⇒ gameInfo.gameRef == sender }
      if (gameEntry.isDefined) {
        val gameInfo = gameEntry.get._2

        for (order ← gameInfo.orders if !order.isBot) {
          accountIdToGameInfo = accountIdToGameInfo - order.accountId
          sendToAccount(order.accountId, AccountLeaveGame)
        }
      }
      Stop
  }

  var accountIdToAccount = Map.empty[AccountId, ActorRef]
  var accountIdToGameOrder = Map.empty[AccountId, GameOrder]
  var accountIdToGameInfo = Map.empty[AccountId, GameInfo]
  var gamesCount = 0

  import context.dispatcher

  val scheduler = context.system.scheduler.schedule(interval, interval, self, TryCreateGames)

  val healthScheduler = context.system.scheduler.schedule(10 seconds, 10 seconds, self, RegisterHealth)

  override val logFilter: Any ⇒ Boolean = {
    case TryCreateGames ⇒ false
    case _ ⇒ true
  }

  def receive = logged {
    case Online(accountId) ⇒
      if ((accountIdToAccount contains accountId) && (accountIdToAccount(accountId) != sender))
        send(accountIdToAccount(accountId), DuplicateAccount)

      accountIdToAccount = accountIdToAccount + (accountId → sender)

    case o@Offline(accountId, client) ⇒
      accountIdToAccount = accountIdToAccount - accountId
      if (accountIdToGameInfo contains accountId) {
        val gameInfo = accountIdToGameInfo(accountId)

        if (gameInfo.isTutor) {
          // Если это тутор и игрок отвалился, то убиваем игру.
          // При перезаходе игрока будет создана новая игра (Иначе новичок не поймет, что произошло)
          accountIdToGameInfo = accountIdToGameInfo - accountId
          stopGame(gameInfo.gameRef)
        } else
          forward(gameInfo.gameRef, o)
      }

    case InGame(accountId) ⇒
      send(sender, InGameResponse(
        gameRef = if (accountIdToGameInfo contains accountId) Some(accountIdToGameInfo(accountId).gameRef) else None,
        searchOpponents = accountIdToGameOrder contains accountId
      ))

    case order: GameOrder ⇒
      val accountId = order.accountId
      if (accountIdToGameInfo contains accountId)
        send(sender, ConnectToGame(accountIdToGameInfo(accountId).gameRef))
      else
        accountIdToGameOrder = accountIdToGameOrder + (accountId → order)

    case TryCreateGames ⇒
      val matchedOrders = matchOrders(accountIdToGameOrder.values.toSeq)
      val newGames = matchedOrders.map(o ⇒ gameCreator.newGame(o, time = System.currentTimeMillis))

      for (newGame ← newGames) {
        val game = gameFactory.create(newGame.gameState, config.isDev, newGame.isTutor, self)
        gamesCount = gamesCount + 1
        val gameInfo = GameInfo(game, newGame.orders, newGame.isTutor)
        if (!newGame.isTutor) send(graphite, createGameStatistics(newGame.orders))

        for (order ← newGame.orders if !order.isBot) {
          accountIdToGameInfo = accountIdToGameInfo + (order.accountId → gameInfo)
          sendToAccount(order.accountId, ConnectToGame(game))
        }
      }

      accountIdToGameOrder = Map.empty

    case PlayerLeaveGame(accountId, place, reward, usedItems) ⇒
      val gameInfo = accountIdToGameInfo(accountId)

      accountIdToGameInfo = accountIdToGameInfo - accountId

      val order = gameInfo.order(accountId)
      val ratingAmount = ELO.ratingAmount(gameInfo.orders, order, place)

      context.actorOf(AccountPatcher.props(accountId, reward, usedItems, ratingAmount, order.userInfo, config, self, storage), "account-patcher-" + accountId.accountType.name + "-" + accountId.id)

      sendToAccount(accountId, AccountLeaveGame)

      val stat = leaveGameStatistics(place, gameInfo.isTutor, gameInfo.orders, order)
      if(stat.isDefined) send(graphite, stat.get)

    case AllPlayersLeaveGame(gameRef) ⇒ stopGame(gameRef)

    case msg: AccountStateUpdated ⇒
      sendToAccount(msg.accountId, msg)

    case RegisterHealth ⇒
      send(graphite, Health(online = accountIdToAccount.size, games = gamesCount))
  }

  def stopGame(gameRef: ActorRef): Unit = {
    context stop gameRef
    gamesCount = gamesCount - 1
  }

  def sendToAccount(accountId: AccountId, msg: Any): Unit =
    if (accountIdToAccount contains accountId)
      send(accountIdToAccount(accountId), msg)
}
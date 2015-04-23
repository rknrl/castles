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
import org.slf4j.LoggerFactory
import ru.rknrl.castles.Config
import ru.rknrl.castles.account.AccountState
import ru.rknrl.castles.database.Database
import ru.rknrl.castles.database.Statistics.{sendCreateGameStatistics, sendLeaveGameStatistics}
import ru.rknrl.castles.matchmaking.MatchMaking._
import ru.rknrl.castles.matchmaking.Matcher.matchOrders
import ru.rknrl.dto._
import ru.rknrl.{Logged, Slf4j}

import scala.concurrent.duration.FiniteDuration

object MatchMaking {

  case class GameOrder(accountId: AccountId,
                       deviceType: DeviceType,
                       userInfo: UserInfoDTO,
                       accountState: AccountState,
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

  case class InGameResponse(gameRef: Option[ActorRef], searchOpponents: Boolean, top: Seq[TopUserInfoDTO])

  case class PlayerLeaveGame(accountId: AccountId, place: Int, reward: Int, usedItems: Map[ItemType, Int])

  case class AllPlayersLeaveGame(gameRef: ActorRef)

  case object DuplicateAccount

  case object TryCreateGames

  case class AccountLeaveGame(top: Seq[TopUserInfoDTO])

  case class SetAccountState(accountId: AccountId, accountState: AccountStateDTO)

}

// supervision

class MatchMaking(gameCreator: GameCreator,
                  gameFactory: IGameFactory,
                  interval: FiniteDuration,
                  var top: Top,
                  config: Config,
                  database: ActorRef,
                  bugs: ActorRef) extends Actor {

  override def supervisorStrategy = OneForOneStrategy() {
    case e: Exception ⇒
      val gameEntry = accountIdToGameInfo.find { case (accountId, gameInfo) ⇒ gameInfo.gameRef == sender }
      if (gameEntry.isDefined) {
        val gameInfo = gameEntry.get._2

        for (order ← gameInfo.orders if !order.isBot) {
          accountIdToGameInfo = accountIdToGameInfo - order.accountId

          if (accountIdToAccount contains order.accountId)
            accountIdToAccount(order.accountId) ! AccountLeaveGame(top.dto)
        }
      }
      Stop
  }

  var accountIdToAccount = Map.empty[AccountId, ActorRef]
  var accountIdToGameOrder = Map.empty[AccountId, GameOrder]
  var accountIdToGameInfo = Map.empty[AccountId, GameInfo]

  import context.dispatcher

  val scheduler = context.system.scheduler.schedule(interval, interval, self, TryCreateGames)

  val log = new Slf4j(LoggerFactory.getLogger(getClass))

  def logged(r: Receive) = new Logged(r, log, None, None, {
    case TryCreateGames ⇒ false
    case _ ⇒ true
  })

  def receive = logged({
    case Online(accountId) ⇒
      if ((accountIdToAccount contains accountId) && (accountIdToAccount(accountId) != sender))
        accountIdToAccount(accountId) ! DuplicateAccount

      accountIdToAccount = accountIdToAccount + (accountId → sender)

    case o@Offline(accountId, client) ⇒
      accountIdToAccount = accountIdToAccount - accountId
      if (accountIdToGameInfo contains accountId) {
        val gameInfo = accountIdToGameInfo(accountId)

        if (gameInfo.isTutor) {
          // Если это тутор и игрок отвалился, то убиваем игру.
          // При перезаходе игрока будет создана новая игра (Иначе новичок не поймет, что произошло)
          accountIdToGameInfo = accountIdToGameInfo - accountId
          context stop gameInfo.gameRef
        } else
          gameInfo.gameRef forward o
      }

    case InGame(accountId) ⇒
      sender ! InGameResponse(
        gameRef = if (accountIdToGameInfo contains accountId) Some(accountIdToGameInfo(accountId).gameRef) else None,
        searchOpponents = accountIdToGameOrder contains accountId,
        top = top.dto
      )

    case order: GameOrder ⇒
      val accountId = order.accountId
      if (accountIdToGameInfo contains accountId)
        sender ! ConnectToGame(accountIdToGameInfo(accountId).gameRef)
      else
        accountIdToGameOrder = accountIdToGameOrder + (accountId → order)

    case TryCreateGames ⇒
      val matchedOrders = matchOrders(accountIdToGameOrder.values.toSeq)
      val newGames = matchedOrders.map(gameCreator.newGame)

      for (newGame ← newGames) {
        val game = gameFactory.create(newGame.gameState, config.isDev, newGame.isTutor, self, bugs)
        val gameInfo = GameInfo(game, newGame.orders, newGame.isTutor)
        if (!newGame.isTutor) sendCreateGameStatistics(newGame.orders, database)

        for (order ← newGame.orders if !order.isBot) {
          accountIdToGameInfo = accountIdToGameInfo + (order.accountId → gameInfo)
          if (accountIdToAccount contains order.accountId)
            accountIdToAccount(order.accountId) ! ConnectToGame(game)
        }
      }

      accountIdToGameOrder = Map.empty

    case PlayerLeaveGame(accountId, place, reward, usedItems) ⇒
      val gameInfo = accountIdToGameInfo(accountId)

      accountIdToGameInfo = accountIdToGameInfo - accountId

      val order = gameInfo.order(accountId)
      val newRating = ELO.newRating(gameInfo.orders, order, place)
      top = top.insert(TopUser(accountId, newRating, order.userInfo))

      context.actorOf(Props(classOf[Patcher], accountId, reward, usedItems, newRating, self, database))

      if (accountIdToAccount contains accountId)
        accountIdToAccount(accountId) ! AccountLeaveGame(top.dto)

      sendLeaveGameStatistics(place, gameInfo.isTutor, gameInfo.orders, order, database)

    case AllPlayersLeaveGame(gameRef) ⇒ context stop gameRef

    case msg@SetAccountState(accountId, _) ⇒
      if (accountIdToAccount contains accountId)
        accountIdToAccount(accountId) forward msg

    case Database.DeleteAccount(accountId) ⇒
      if (accountIdToAccount contains accountId)
        accountIdToAccount(accountId) ! DuplicateAccount
  })
}
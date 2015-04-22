//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.matchmaking

import akka.actor.{Actor, ActorRef}
import ru.rknrl.castles.account.AccountState
import ru.rknrl.castles.matchmaking.NewMatchmaking._
import ru.rknrl.dto._

object NewMatchmaking {

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

  case object AccountLeaveGame

  case class SetAccountState(accountId: AccountId, accountState: AccountStateDTO)

}

class NewMatchmaking(gamesFactory: IGamesFactory,
                     var top: Top) extends Actor {

  var accountIdToAccount = Map.empty[AccountId, ActorRef]
  var accountIdToGameOrder = Map.empty[AccountId, GameOrder]
  var accountIdToGameInfo = Map.empty[AccountId, GameInfo]

  def receive = {
    case Online(accountId) ⇒
      if ((accountIdToAccount contains accountId) && (accountIdToAccount(accountId) != sender))
        accountIdToAccount(accountId) ! DuplicateAccount

      accountIdToAccount = accountIdToAccount + (accountId → sender)

    case o@Offline(accountId, client) ⇒
      accountIdToAccount = accountIdToAccount - accountId
      if (accountIdToGameInfo contains accountId)
        accountIdToGameInfo(accountId).gameRef forward o

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
      val newAccountIdToGameInfo = gamesFactory.createGames(accountIdToGameOrder, self)
      accountIdToGameOrder = Map.empty

      accountIdToGameInfo = accountIdToGameInfo ++ newAccountIdToGameInfo

      for ((accountId, gameInfo) ← newAccountIdToGameInfo)
        if (accountIdToAccount contains accountId)
          accountIdToAccount(accountId) ! ConnectToGame(gameInfo.gameRef)

    case PlayerLeaveGame(accountId, place, reward, usedItems) ⇒
      accountIdToGameInfo = accountIdToGameInfo - accountId

    case AllPlayersLeaveGame(gameRef) ⇒
      accountIdToGameInfo = accountIdToGameInfo.filter { case (accountId, gameInfo) ⇒ gameInfo.gameRef != gameRef }
      context stop gameRef
  }
}
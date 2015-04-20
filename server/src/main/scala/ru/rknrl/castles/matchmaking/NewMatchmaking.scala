//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.matchmaking

import akka.actor.{Actor, ActorRef}
import ru.rknrl.castles.matchmaking.NewMatchmaking._
import ru.rknrl.dto.{AccountId, TopUserInfoDTO}

object NewMatchmaking {

  case class GameOrderNew(accountId: AccountId)

  case class GameInfoNew(ref: ActorRef)


  case class Online(accountId: AccountId)

  case class Offline(accountId: AccountId, client: ActorRef)

  case class ConnectToGame(gameRef: ActorRef)

  case class InGame(accountId: AccountId)

  case class InGameResponse(gameRef: Option[ActorRef], searchOpponents: Boolean, top: Seq[TopUserInfoDTO])

  case class PlayerLeaveGameNew(accountId: AccountId)

  case class AllPlayersLeaveGame(gameRef: ActorRef)

  case object DuplicateAccount

  case object TryCreateGames

}

class NewMatchmaking(gamesFactory: IGamesFactory,
                     var top: Top) extends Actor {

  var accountIdToAccount = Map.empty[AccountId, ActorRef]
  var accountIdToGameOrder = Map.empty[AccountId, GameOrderNew]
  var accountIdToGameInfo = Map.empty[AccountId, GameInfoNew]

  def receive = {
    case Online(accountId) ⇒
      if ((accountIdToAccount contains accountId) && (accountIdToAccount(accountId) != sender))
        accountIdToAccount(accountId) ! DuplicateAccount

      accountIdToAccount = accountIdToAccount + (accountId → sender)

    case o@Offline(accountId, client) ⇒
      accountIdToAccount = accountIdToAccount - accountId
      if (accountIdToGameInfo contains accountId)
        accountIdToGameInfo(accountId).ref forward o

    case InGame(accountId) ⇒
      sender ! InGameResponse(
        gameRef = if (accountIdToGameInfo contains accountId) Some(accountIdToGameInfo(accountId).ref) else None,
        searchOpponents = accountIdToGameOrder contains accountId,
        top = top.dto
      )

    case order@GameOrderNew(accountId) ⇒
      if (accountIdToGameInfo contains accountId)
        sender ! ConnectToGame(accountIdToGameInfo(accountId).ref)
      else
        accountIdToGameOrder = accountIdToGameOrder + (accountId → order)

    case TryCreateGames ⇒
      val newAccountIdToGameInfo = gamesFactory.createGames(accountIdToGameOrder, self)
      accountIdToGameOrder = Map.empty

      accountIdToGameInfo = accountIdToGameInfo ++ newAccountIdToGameInfo

      for ((accountId, gameInfo) ← newAccountIdToGameInfo)
        if (accountIdToAccount contains accountId)
          accountIdToAccount(accountId) ! ConnectToGame(gameInfo.ref)

    case PlayerLeaveGameNew(accountId) ⇒
      accountIdToGameInfo = accountIdToGameInfo - accountId

    case AllPlayersLeaveGame(gameRef) ⇒
      accountIdToGameInfo = accountIdToGameInfo.filter { case (accountId, gameInfo) ⇒ gameInfo.ref != gameRef }
      context stop gameRef
  }
}
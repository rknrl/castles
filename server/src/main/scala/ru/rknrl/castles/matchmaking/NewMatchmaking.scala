//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.matchmaking

import akka.actor.{Actor, ActorRef}
import ru.rknrl.castles.game.GameScheduler
import ru.rknrl.castles.kit.Mocks
import ru.rknrl.castles.matchmaking.NewMatchmaking._
import ru.rknrl.dto.AccountId

object NewMatchmaking {

  case class Online(accountId: AccountId)

  case class Offline(accountId: AccountId, client: ActorRef)

  case class GameOrder(accountId: AccountId)

  case class ConnectToGame(gameRef: ActorRef)

  case class InGame(accountId: AccountId)

  case class InGameResponse(searchOpponents: Boolean, gameRef: Option[ActorRef])

  case class PlayerLeaveGame(accountId: AccountId)

  case class AllPlayersLeaveGame(gameRef: ActorRef)

  case object DuplicateAccount

  case object TryCreateGames

}

class NewMatchmaking(gameFactory: IGameFactory) extends Actor {

  var accountIdToAccount = Map.empty[AccountId, ActorRef]
  var accountIdToGameOrder = Map.empty[AccountId, GameOrder]
  var accountIdToGame = Map.empty[AccountId, ActorRef]

  def receive = {
    case Online(accountId) ⇒
      if ((accountIdToAccount contains accountId) && (accountIdToAccount(accountId) != sender))
        accountIdToAccount(accountId) ! DuplicateAccount

      accountIdToAccount = accountIdToAccount + (accountId → sender)

    case o@Offline(accountId, client) ⇒
      accountIdToAccount = accountIdToAccount - accountId
      if (accountIdToGame contains accountId)
        accountIdToGame(accountId) forward o

    case InGame(accountId) ⇒
      sender ! InGameResponse(
        searchOpponents = accountIdToGameOrder contains accountId,
        gameRef = if (accountIdToGame contains accountId) Some(accountIdToGame(accountId)) else None
      )

    case order@GameOrder(accountId) ⇒
      if (accountIdToGame contains accountId)
        sender ! ConnectToGame(accountIdToGame(accountId))
      else
        accountIdToGameOrder = accountIdToGameOrder + (accountId → order)

    case TryCreateGames ⇒
      for ((accountId, order) ← accountIdToGameOrder) {
        val game = gameFactory.create(
          gameState = Mocks.gameStateMock(),
          isDev = true,
          schedulerClass = classOf[GameScheduler],
          matchmaking = self,
          bugs = self
        )
        accountIdToGame = accountIdToGame + (accountId → game)

        if (accountIdToAccount contains accountId)
          accountIdToAccount(accountId) ! ConnectToGame(game)
      }

      accountIdToGameOrder = Map.empty

    case PlayerLeaveGame(accountId) ⇒
      accountIdToGame = accountIdToGame - accountId

    case AllPlayersLeaveGame(gameRef) ⇒
      accountIdToGame = accountIdToGame.filter { case (accountId, game) ⇒ game != gameRef }
      context stop gameRef
  }
}
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
import ru.rknrl.castles.database.Database.{AccountStateResponse, GetAccountState, UpdateAccountState}
import ru.rknrl.castles.matchmaking.MatchMaking.SetAccountState
import ru.rknrl.dto.{AccountId, ItemType}
import ru.rknrl.logging.ActorLog

class AccountPatcher(accountId: AccountId,
                     reward: Int,
                     usedItems: Map[ItemType, Int],
                     newRating: Double,
                     matchmaking: ActorRef,
                     database: ActorRef) extends Actor with ActorLog {

  send(database, GetAccountState(accountId))

  def receive = logged {
    case AccountStateResponse(accountId, stateDto, ratingOption) ⇒
      val state = AccountState.fromDto(stateDto, newRating)

      val newState = state.addGold(reward)
        .incGamesCount
        .setNewRating(newRating)
        .applyUsedItems(usedItems)

      send(database, UpdateAccountState(accountId, newState.dto, newState.rating))

      become(waitForUpdatedState, "waitForUpdatedState")
  }

  def waitForUpdatedState: Receive = logged {
    case AccountStateResponse(accountId, stateDto, rating) ⇒
      send(matchmaking, SetAccountState(accountId, stateDto, newRating))
      context stop self
  }
}

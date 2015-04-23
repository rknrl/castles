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

class Patcher(accountId: AccountId,
              reward: Int,
              usedItems: Map[ItemType, Int],
              newRating: Double,
              matchmaking: ActorRef,
              database: ActorRef) extends Actor {

  database ! GetAccountState(accountId)

  var updated: Boolean = false

  def receive = {
    case AccountStateResponse(accountId, stateDto) ⇒
      if (!updated) {
        val state = AccountState(stateDto)

        val newState = state.addGold(reward)
          .incGamesCount
          .setNewRating(newRating)
          .applyUsedItems(usedItems)

        database ! UpdateAccountState(accountId, newState.dto)
        updated = true
      } else {
        matchmaking ! SetAccountState(accountId, stateDto)
        context stop self
      }
  }
}

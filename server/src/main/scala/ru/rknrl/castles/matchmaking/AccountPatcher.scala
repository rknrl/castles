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
import ru.rknrl.castles.database.Database._
import ru.rknrl.castles.matchmaking.MatchMaking.{SetAccountState, SetRating}
import ru.rknrl.dto.{AccountId, ItemType}
import ru.rknrl.logging.ActorLog

class AccountPatcher(accountId: AccountId,
                     reward: Int,
                     usedItems: Map[ItemType, Int],
                     newRating: Double,
                     matchmaking: ActorRef,
                     database: ActorRef) extends Actor with ActorLog {

  send(database, UpdateRating(accountId, newRating))

  def receive = waitForUpdatedRating

  def waitForUpdatedRating: Receive = logged {
    case RatingResponse(accountId, rating) ⇒
      send(database, GetAccountState(accountId))
      become(waitForState, "waitForState")
  }

  def waitForState: Receive = logged {
    case AccountStateResponse(accountId, stateDto) ⇒
      val state = AccountState(stateDto)

      val newState = state.addGold(reward)
        .incGamesCount
        .applyUsedItems(usedItems)

      send(database, UpdateAccountState(accountId, newState.dto))
      become(waitForUpdatedState, "waitForUpdatedState")
  }

  def waitForUpdatedState: Receive = logged {
    case AccountStateResponse(accountId, stateDto) ⇒
      send(matchmaking, SetRating(accountId, newRating))
      send(matchmaking, SetAccountState(accountId, stateDto))
      context stop self
  }
}

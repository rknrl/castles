//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.matchmaking

import akka.actor.{Actor, ActorRef}
import ru.rknrl.castles.Config
import ru.rknrl.castles.account.AccountState
import ru.rknrl.castles.database.Database._
import ru.rknrl.castles.matchmaking.MatchMaking.{SetAccountState, SetRating}
import ru.rknrl.dto.{AccountId, ItemType}
import ru.rknrl.logging.ActorLog

class AccountPatcher(accountId: AccountId,
                     reward: Int,
                     usedItems: Map[ItemType, Int],
                     newRating: Double,
                     config: Config,
                     matchmaking: ActorRef,
                     database: ActorRef) extends Actor with ActorLog {

  send(database, UpdateRating(accountId, newRating))

  var place = 0L

  def receive = waitForUpdatedRating

  def waitForUpdatedRating: Receive = logged {
    case RatingResponse(accountId, rating) ⇒
      if (rating.isEmpty) throw new IllegalStateException("updated rating is empty")

      send(database, GetPlace(rating.get))
      become(waitForPlace, "waitForPlace")
  }

  def waitForPlace: Receive = logged {
    case PlaceResponse(place) ⇒
      this.place = place
      send(database, GetAccountState(accountId))
      become(waitForState, "waitForState")
  }

  def waitForState: Receive = logged {
    case AccountStateResponse(accountId, stateDto) ⇒
      val state = if (stateDto.isDefined) AccountState(stateDto.get) else config.account.initAccount

      val newState = state.addGold(reward)
        .incGamesCount
        .applyUsedItems(usedItems)

      send(database, UpdateAccountState(accountId, newState.dto))
      become(waitForUpdatedState, "waitForUpdatedState")
  }

  def waitForUpdatedState: Receive = logged {
    case AccountStateResponse(accountId, stateDto) ⇒
      if (stateDto.isEmpty) throw new IllegalStateException("AccountState is empty after update")
      send(matchmaking, SetRating(accountId, newRating, place))
      send(matchmaking, SetAccountState(accountId, stateDto.get))
      context stop self
  }
}

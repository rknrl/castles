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
import ru.rknrl.dto.{AccountId, AccountStateDTO, ItemType}
import ru.rknrl.logging.ActorLog

class AccountPatcher(accountId: AccountId,
                     reward: Int,
                     usedItems: Map[ItemType, Int],
                     newRating: Double,
                     config: Config,
                     matchmaking: ActorRef,
                     databaseQueue: ActorRef) extends Actor with ActorLog {

  val transform = (stateDto: Option[AccountStateDTO], ratingDto: Option[Double]) ⇒ {
    val state = stateDto.getOrElse(config.account.initState)

    val newState = AccountState.addGold(
      AccountState.incGamesCount(
        AccountState.applyUsedItems(
          state,
          usedItems
        )
      ),
      reward
    )

    (newState, newRating)
  }

  send(databaseQueue, GetAndUpdateAccountStateAndRating(accountId, transform))

  def receive = logged {
    case AccountStateAndRatingResponse(accountId, stateDto, newRating, place) ⇒
      send(matchmaking, SetRating(accountId, newRating, place))
      send(matchmaking, SetAccountState(accountId, stateDto))
      context stop self
  }
}

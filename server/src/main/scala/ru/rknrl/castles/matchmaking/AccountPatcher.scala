//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.matchmaking

import akka.actor.{Actor, ActorRef, Props}
import protos._
import ru.rknrl.castles.Config
import ru.rknrl.castles.account.AccountState
import ru.rknrl.castles.database.Database._
import ru.rknrl.logging.ShortActorLogging

object AccountPatcher {
  def props(accountId: AccountId,
            reward: Int,
            usedItems: Map[ItemType, Int],
            ratingAmount: Double,
            userInfo: UserInfo,
            config: Config,
            matchmaking: ActorRef,
            databaseQueue: ActorRef) =
    Props(
      classOf[AccountPatcher],
      accountId,
      reward,
      usedItems,
      ratingAmount,
      userInfo,
      config,
      matchmaking,
      databaseQueue
    )
}

class AccountPatcher(accountId: AccountId,
                     reward: Int,
                     usedItems: Map[ItemType, Int],
                     ratingAmount: Double,
                     userInfo: UserInfo,
                     config: Config,
                     matchmaking: ActorRef,
                     databaseQueue: ActorRef) extends Actor with ShortActorLogging {

  val transform = (stateDto: Option[protos.AccountState], ratingDto: Option[Double]) ⇒ {
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

    (newState, ratingDto.getOrElse(1400.0) + ratingAmount)
  }

  send(databaseQueue, GetAndUpdateAccountStateAndRating(accountId, transform, userInfo))

  def receive = logged {
    case msg: AccountStateAndRatingResponse ⇒
      send(matchmaking, msg)
      context stop self
  }
}

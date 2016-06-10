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
import ru.rknrl.castles.account.AccountState.{addGold, applyUsedItems, incGamesCount}
import ru.rknrl.castles.storage.Storage._
import ru.rknrl.logging.ShortActorLogging

object AccountPatcher {
  def props(accountId: AccountId,
            reward: Int,
            usedItems: Map[ItemType, Int],
            ratingAmount: Double,
            userInfo: UserInfo,
            config: Config,
            matchmaking: ActorRef,
            storage: ActorRef) =
    Props(
      classOf[AccountPatcher],
      accountId,
      reward,
      usedItems,
      ratingAmount,
      userInfo,
      config,
      matchmaking,
      storage
    )
}

class AccountPatcher(accountId: AccountId,
                     reward: Int,
                     usedItems: Map[ItemType, Int],
                     ratingAmount: Double,
                     userInfo: UserInfo,
                     config: Config,
                     matchmaking: ActorRef,
                     storage: ActorRef) extends Actor with ShortActorLogging {

  val transform = (stateDto: Option[protos.AccountState], ratingDto: Option[Double]) ⇒ {
    val state = stateDto.getOrElse(config.account.initState)

    val newState = addGold(
      incGamesCount(
        applyUsedItems(
          state,
          usedItems
        )
      ),
      reward
    )

    (newState, ratingDto.getOrElse(1400.0) + ratingAmount)
  }

  send(storage, GetAndUpdateAccountStateAndRating(accountId, transform, userInfo))

  def receive = logged {
    case msg: AccountStateAndRatingUpdated ⇒
      send(matchmaking, msg)
      context stop self
  }
}

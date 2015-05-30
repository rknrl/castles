//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.matchmaking

import akka.actor.{Actor, ActorRef, Props}
import ru.rknrl.castles.Config
import ru.rknrl.castles.account.AccountState
import ru.rknrl.castles.database.DatabaseTransaction._
import ru.rknrl.dto.{AccountId, AccountStateDTO, ItemType, UserInfoDTO}
import ru.rknrl.logging.ActorLog

object AccountPatcher {
  def props(accountId: AccountId,
            reward: Int,
            usedItems: Map[ItemType, Int],
            ratingAmount: Double,
            userInfo: UserInfoDTO,
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
                     userInfo: UserInfoDTO,
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

    (newState, ratingDto.getOrElse(1400.0) + ratingAmount)
  }

  send(databaseQueue, GetAndUpdateAccountStateAndRating(accountId, transform, userInfo))

  def receive = logged {
    case msg: AccountStateAndRatingResponse ⇒
      send(matchmaking, msg)
      context stop self
  }
}

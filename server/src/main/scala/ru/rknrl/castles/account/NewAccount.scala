//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account

import akka.actor.{Actor, ActorRef}
import ru.rknrl.castles.matchmaking.NewMatchmaking.{Offline, DuplicateAccount}
import ru.rknrl.dto.AccountId
import ru.rknrl.dto.AccountType.VKONTAKTE

class NewAccount(matchmaking: ActorRef) extends Actor {
  def receive = {
    case DuplicateAccount ⇒ context stop self
  }

  override def postStop(): Unit =
    matchmaking ! Offline(AccountId(VKONTAKTE, "1"))
}

//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.database

import akka.actor.{Actor, ActorRef}
import ru.rknrl.castles.database.Accounts.{IsReady, Ready}
import ru.rknrl.castles.database.Database.AccountMsg
import ru.rknrl.dto.AccountId

class AccountsQueue(accounts: ActorRef) extends Actor {
  var queues = Map.empty[AccountId, List[AccountMsg]]

  def receive = {
    case msg: AccountMsg ⇒
      val newList = queues.getOrElse(msg.accountId, List.empty) :+ msg
      queues = queues + (msg.accountId → newList)
      accounts ! IsReady(msg.accountId)

    case Ready(accountId) ⇒
      if (queues contains accountId) {
        val list = queues(accountId)
        accounts ! list.head
        if (list.size > 1) {
          val newList = queues(accountId) drop 1
          queues = queues + (accountId → newList)
        } else
          queues = queues - accountId
      }
  }
}

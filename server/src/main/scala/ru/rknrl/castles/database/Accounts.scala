//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.database

import akka.actor.{Actor, ActorRef}
import ru.rknrl.castles.database.Accounts._
import ru.rknrl.castles.database.Database._
import ru.rknrl.dto.{AccountId, AccountStateDTO}
import ru.rknrl.logging.ActorLog

object Accounts {

  case class Get(accountId: AccountId, receiver: ActorRef) extends AccountMsg

  case class GetAndUpdate(accountId: AccountId, receiver: ActorRef, transform: Option[AccountStateDTO] ⇒ AccountStateDTO) extends AccountMsg

  case class Ready(accountId: AccountId)

  case class IsReady(accountId: AccountId)

}

class Accounts(database: ActorRef, queue: ActorRef) extends Actor with ActorLog {
  var accountIdToTransform = Map.empty[AccountId, Option[AccountStateDTO] ⇒ AccountStateDTO]
  var accountIdToSender = Map.empty[AccountId, ActorRef]

  def receive = {
    case Get(accountId, receiver) ⇒
      check(accountId)
      get(accountId, receiver)

    case GetAndUpdate(accountId, receiver, transform) ⇒
      check(accountId)
      accountIdToTransform = accountIdToTransform + (accountId → transform)
      get(accountId, receiver)

    case msg@AccountStateResponse(accountId, state) ⇒
      if (accountIdToTransform contains accountId) {
        val transform = accountIdToTransform(accountId)
        val newState = transform(state)
        send(database, UpdateAccountState(accountId, newState))
      } else
        complete(accountId, msg)

    case msg@AccountStateUpdated(accountId, state) ⇒ complete(accountId, msg)

    case IsReady(accountId) ⇒
      if (!(accountIdToSender contains accountId))
        send(sender, Ready(accountId))
  }

  def check(accountId: AccountId): Unit = {
    if (accountIdToTransform contains accountId) throw new IllegalStateException("transform already exists for " + accountId)
    if (accountIdToSender contains accountId) throw new IllegalStateException("sender already exists for " + accountId)
  }

  def get(accountId: AccountId, sender: ActorRef): Unit = {
    accountIdToSender = accountIdToSender + (accountId → sender)
    send(database, GetAccountState(accountId))
  }

  def complete(accountId: AccountId, msg: Any): Unit = {
    send(accountIdToSender(accountId), msg)
    accountIdToTransform = accountIdToTransform - accountId
    accountIdToSender = accountIdToSender - accountId
    send(queue, Ready(accountId))
  }
}
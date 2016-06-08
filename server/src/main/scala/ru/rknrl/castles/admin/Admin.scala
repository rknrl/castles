//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.admin

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import protos._
import ru.rknrl.castles.Config
import ru.rknrl.castles.storage.Storage._
import ru.rknrl.logging.ShortActorLogging

object Admin {
  def props(client: ActorRef,
            storage: ActorRef,
            matchmaking: ActorRef,
            config: Config) =
    Props(classOf[Admin], client, storage, matchmaking, config)
}

class Admin(client: ActorRef,
            storage: ActorRef,
            matchmaking: ActorRef,
            config: Config) extends Actor with ShortActorLogging {

  var accountId: Option[AccountId] = None

  def receive = auth

  def auth: Receive = logged {
    case authenticate: AdminAuthenticate ⇒
      if (authenticate.login == config.adminLogin && authenticate.password == config.adminPassword) {
        send(client, AuthenticatedAsAdmin())
        become(admin, "admin")
      } else {
        log.debug("reject")
        send(sender, PoisonPill)
      }
  }

  def admin: Receive = logged {
    case dto: AdminGetAccountState ⇒
      accountId = Some(dto.accountId)
      send(storage, GetAccount(dto.accountId))
      become(waitForState, "waitForState")

    case AdminSetAccountState(accountState) ⇒
      send(storage, GetAndUpdateAccountState(accountId.get, oldState ⇒ accountState)) // todo checkAndUpdate
      become(waitForUpdatedState, "waitForUpdatedState")
  }

  def waitForState = logged {
    case msg: AccountResponse ⇒
      if (msg.state.isDefined)
        send(client, AdminAccountState(accountId.get, msg.state.get))
      become(admin, "admin")
  }

  def waitForUpdatedState = logged {
    case msg@AccountStateUpdated(_, accountState) ⇒
      send(client, AdminAccountState(accountId.get, accountState))
      send(matchmaking, msg)
      become(admin, "admin")
  }
}
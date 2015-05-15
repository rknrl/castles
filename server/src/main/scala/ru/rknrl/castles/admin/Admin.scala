//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.admin

import akka.actor.{Actor, ActorRef, Props}
import ru.rknrl.castles.Config
import ru.rknrl.castles.database.Database
import ru.rknrl.castles.database.Database.{AccountResponse, AccountStateResponse}
import ru.rknrl.castles.matchmaking.MatchMaking.SetAccountState
import ru.rknrl.castles.rmi.B2C.{AdminAccountState, AuthenticatedAsAdmin}
import ru.rknrl.castles.rmi.C2B._
import ru.rknrl.core.rmi.CloseConnection
import ru.rknrl.dto.{AccountId, AdminAccountStateDTO}
import ru.rknrl.logging.ActorLog

object Admin {
  def props(databaseQueue: ActorRef,
            matchmaking: ActorRef,
            config: Config) =
    Props(classOf[Admin], databaseQueue, matchmaking, config)
}

class Admin(databaseQueue: ActorRef,
            matchmaking: ActorRef,
            config: Config) extends Actor with ActorLog {

  var client: Option[ActorRef] = None

  var accountId: Option[AccountId] = None

  override def receive = auth

  def auth: Receive = logged {
    case AuthenticateAsAdmin(authenticate) ⇒
      if (authenticate.login == config.adminLogin && authenticate.password == config.adminPassword) {
        client = Some(sender)
        send(client.get, AuthenticatedAsAdmin)
        become(admin, "admin")
      } else {
        log.debug("reject")
        send(sender, CloseConnection)
      }
  }

  def admin: Receive = logged {
    case AdminGetAccountState(dto) ⇒
      accountId = Some(dto.accountId)
      send(databaseQueue, Database.GetAccount(dto.accountId))
      become(waitForState, "waitForState")

    case AdminSetAccountState(accountState) ⇒
      send(databaseQueue, Database.GetAndUpdateAccountState(accountId.get, oldState ⇒ accountState)) // todo checkAndUpdate
      become(waitForUpdatedState, "waitForUpdatedState")
  }

  def waitForState = logged {
    case msg: AccountResponse ⇒
      if (msg.state.isDefined)
        send(client.get, AdminAccountState(AdminAccountStateDTO(accountId.get, msg.state.get)))
      become(admin, "admin")
  }

  def waitForUpdatedState = logged {
    case AccountStateResponse(_, accountState) ⇒
      send(client.get, AdminAccountState(AdminAccountStateDTO(accountId.get, accountState)))
      send(matchmaking, SetAccountState(accountId.get, accountState))
      become(admin, "admin")
  }
}
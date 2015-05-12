//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.admin

import akka.actor.{Actor, ActorRef}
import ru.rknrl.castles.Config
import ru.rknrl.castles.database.Database
import ru.rknrl.castles.database.Database.AccountStateResponse
import ru.rknrl.castles.matchmaking.MatchMaking.SetAccountState
import ru.rknrl.castles.rmi.B2C.{AdminAccountState, AuthenticatedAsAdmin}
import ru.rknrl.castles.rmi.C2B._
import ru.rknrl.core.rmi.CloseConnection
import ru.rknrl.dto.{AccountId, AdminAccountStateDTO}
import ru.rknrl.logging.ActorLog

class Admin(database: ActorRef,
            matchmaking: ActorRef,
            config: Config,
            name: String) extends Actor with ActorLog {

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
      send(database, Database.GetAccountState(dto.accountId))
      become(waitForState, "waitForState")

    case AdminSetAccountState(accountState) ⇒
      send(database, Database.UpdateAccountState(accountId.get, accountState))
      become(waitForUpdatedState, "waitForUpdatedState")
  }

  def waitForState = logged {
    case AccountStateResponse(accountState) ⇒
      if (accountState.isDefined)
        send(client.get, AdminAccountState(AdminAccountStateDTO(accountId.get, accountState.get)))
      become(admin, "admin")
  }

  def waitForUpdatedState = logged {
    case AccountStateResponse(accountState) ⇒
      send(client.get, AdminAccountState(AdminAccountStateDTO(accountId.get, accountState.get)))
      send(matchmaking, SetAccountState(accountId.get, accountState.get))
      become(admin, "admin")
  }
}
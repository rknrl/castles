//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.admin

import akka.actor.{Actor, ActorRef}
import akka.pattern.Patterns
import ru.rknrl.castles.account.AccountState
import ru.rknrl.castles.database.Database
import ru.rknrl.castles.database.Database.{AccountNoExists, AccountStateResponse, GetAccountState, UpdateAccountState}
import ru.rknrl.castles.matchmaking.MatchMaking.SetAccountState
import ru.rknrl.castles.rmi.B2C.AuthenticatedAsAdmin
import ru.rknrl.castles.rmi.C2B._
import ru.rknrl.castles.rmi._
import ru.rknrl.core.rmi.CloseConnection
import ru.rknrl.dto.{AccountId, AccountStateDTO, AdminAccountStateDTO}
import ru.rknrl.logging.ActorLog

import scala.concurrent.Await
import scala.concurrent.duration._

// todo: ask logging

class Admin(database: ActorRef,
            matchmaking: ActorRef,
            login: String,
            password: String,
            name: String) extends Actor with ActorLog {

  var client: ActorRef = null

  override def receive = auth

  def auth: Receive = logged {
    case AuthenticateAsAdmin(authenticate) ⇒
      if (authenticate.login == login && authenticate.password == password) {
        client = sender
        send(client, AuthenticatedAsAdmin)
        become(admin, "admin")
      } else {
        log.debug("reject")
        send(sender, CloseConnection)
      }
  }

  def admin: Receive = logged {
    case AccountStateResponse(accountId, accountState) ⇒
      sendToClient(accountId, accountState)

    case AccountNoExists ⇒

    case C2B.GetAccountState(dto) ⇒
      send(database, Database.GetAccountState(dto.accountId))

    case C2B.DeleteAccount(accountId) ⇒
      send(database, Database.DeleteAccount(accountId))

    case msg: Database.AccountDeleted ⇒
      forward(matchmaking, msg)

    case AddGold(dto) ⇒
      getState(dto.accountId,
        (accountId, accountState) ⇒ update(accountId, accountState.addGold(dto.amount))
      )

    case AddItem(dto) ⇒
      getState(dto.accountId,
        (accountId, accountState) ⇒ update(accountId, accountState.addItem(dto.itemType, dto.amount))
      )

    case SetSkill(dto) ⇒
      getState(dto.accountId,
        (accountId, accountState) ⇒ update(accountId, accountState.setSkill(dto.skilType, dto.skillLevel))
      )

    case SetSlot(dto) ⇒
      getState(dto.accountId,
        (accountId, accountState) ⇒
          if (dto.slot.buildingPrototype.isDefined)
            update(accountId, accountState.setBuilding(dto.slot.id, dto.slot.buildingPrototype.get))
          else
            update(accountId, accountState.removeBuilding(dto.slot.id))
      )
  }

  def sendToClient(accountId: AccountId, accountState: AccountStateDTO) =
    send(client, B2C.AccountState(AdminAccountStateDTO(accountId, accountState)))

  def getState(accountId: AccountId, f: (AccountId, AccountState) ⇒ Unit) = {
    val future = Patterns.ask(database, GetAccountState(accountId), 5 seconds)
    val result = Await.result(future, 5 seconds)

    result match {
      case AccountStateResponse(accountId, accountStateDto) ⇒
        f(accountId, AccountState(accountStateDto))

      case invalid ⇒
        log.error("invalid result=" + invalid)
    }
  }

  def update(accountId: AccountId, newAccountState: AccountState): Unit = {
    val future = Patterns.ask(database, UpdateAccountState(accountId, newAccountState.dto), 5 seconds)
    val result = Await.result(future, 5 seconds)

    result match {
      case AccountStateResponse(accountId, accountStateDto) ⇒
        send(matchmaking, SetAccountState(accountId, accountStateDto))
        sendToClient(accountId, accountStateDto)

      case invalid ⇒
        log.error("invalid result=" + invalid)
    }
  }
}
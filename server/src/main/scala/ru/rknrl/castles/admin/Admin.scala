//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.admin

import akka.actor.{ActorLogging, ActorRef}
import akka.pattern.Patterns
import ru.rknrl.EscalateStrategyActor
import ru.rknrl.castles.AccountId
import ru.rknrl.castles.MatchMaking.AdminSetAccountState
import ru.rknrl.castles.account.state.{AccountState, BuildingPrototype}
import ru.rknrl.castles.database.Database.{Get, NoExist, StateResponse, Update}
import ru.rknrl.castles.rmi.B2C.{AdminOnline, AuthenticatedAsAdmin}
import ru.rknrl.castles.rmi.C2B._
import ru.rknrl.castles.rmi._
import ru.rknrl.core.rmi.CloseConnection
import ru.rknrl.dto.AccountDTO.AccountStateDTO
import ru.rknrl.dto.AdminDTO.AdminAccountStateDTO
import ru.rknrl.dto.CommonDTO.AccountIdDTO

import scala.concurrent.Await
import scala.concurrent.duration._

class Admin(database: ActorRef,
            matchmaking: ActorRef,
            login: String,
            password: String,
            name: String) extends EscalateStrategyActor with ActorLogging {

  var client: ActorRef = null

  override def receive = auth

  def auth: Receive = {
    /** from Client */
    case AuthenticateAsAdmin(authenticate) ⇒
      if (authenticate.getLogin == login && authenticate.getPassword == password) {
        client = sender
        client ! AuthenticatedAsAdmin
        context become admin
      } else {
        log.info("reject")
        sender ! CloseConnection
      }
  }

  def admin: Receive = {
    /** from Database */
    case StateResponse(accountId, accountState) ⇒ sendToClient(accountId, accountState)

    /** from Database */
    case NoExist ⇒ log.info("account does not exist")

    case GetAccountState(dto) ⇒
      database ! Get(dto.getAccountId)

    case GetOnline ⇒
      log.info("getOnline")
      matchmaking ! GetOnline

    case msg: AdminOnline ⇒
      log.info("adminOnline")
      client ! msg

    case AddGold(dto) ⇒
      getState(dto.getAccountId,
        (accountId, accountState) ⇒ update(accountId, accountState.addGold(dto.getAmount))
      )

    case AddItem(dto) ⇒
      getState(dto.getAccountId,
        (accountId, accountState) ⇒ update(accountId, accountState.addItem(dto.getItemType, dto.getAmount))
      )

    case SetSkill(dto) ⇒
      getState(dto.getAccountId,
        (accountId, accountState) ⇒ update(accountId, accountState.setSkill(dto.getSkilType, dto.getSkillLevel))
      )

    case SetSlot(dto) ⇒
      getState(dto.getAccountId,
        (accountId, accountState) ⇒
          if (dto.getSlot.hasBuildingPrototype)
            update(accountId, accountState.setBuilding(dto.getSlot.getId, BuildingPrototype.fromDto(dto.getSlot.getBuildingPrototype)))
          else
            update(accountId, accountState.removeBuilding(dto.getSlot.getId))
      )
  }

  def sendToClient(accountId: AccountIdDTO, accountState: AccountStateDTO) =
    client ! B2C.AccountState(
      AdminAccountStateDTO.newBuilder()
        .setAccountId(accountId)
        .setAccountState(accountState)
        .build())

  def getState(accountId: AccountIdDTO, f: (AccountIdDTO, AccountState) ⇒ Unit) = {
    val future = Patterns.ask(database, Get(accountId), 5 seconds)
    val result = Await.result(future, 5 seconds)

    result match {
      case StateResponse(accountId, accountStateDto) ⇒
        f(accountId, AccountState.fromDto(accountStateDto))

      case invalid ⇒
        log.info("invalid result=" + invalid)
    }
  }

  def update(accountId: AccountIdDTO, newAccountState: AccountState): Unit = {
    val future = Patterns.ask(database, Update(accountId, newAccountState.dto), 5 seconds)
    val result = Await.result(future, 5 seconds)

    result match {
      case StateResponse(accountId, accountStateDto) ⇒
        matchmaking ! AdminSetAccountState(new AccountId(accountId), accountStateDto)
        sendToClient(accountId, accountStateDto)

      case invalid ⇒
        log.info("invalid result=" + invalid)
    }
  }
}
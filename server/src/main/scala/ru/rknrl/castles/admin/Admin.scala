//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.admin

import akka.actor.ActorRef
import akka.pattern.Patterns
import org.slf4j.LoggerFactory
import ru.rknrl.castles.AccountId
import ru.rknrl.castles.MatchMaking.AdminSetAccountState
import ru.rknrl.castles.account.state.{AccountState, BuildingPrototype}
import ru.rknrl.castles.database.Database
import ru.rknrl.castles.database.Database.{AccountNoExists, AccountStateResponse, GetAccountState, UpdateAccountState}
import ru.rknrl.castles.rmi.B2C.{AuthenticatedAsAdmin, ServerHealth}
import ru.rknrl.castles.rmi.C2B._
import ru.rknrl.castles.rmi._
import ru.rknrl.core.rmi.CloseConnection
import ru.rknrl.dto.AccountDTO.AccountStateDTO
import ru.rknrl.dto.AdminDTO.AdminAccountStateDTO
import ru.rknrl.dto.CommonDTO.AccountIdDTO
import ru.rknrl.{EscalateStrategyActor, Logged, Slf4j}

import scala.concurrent.Await
import scala.concurrent.duration._

class Admin(database: ActorRef,
            matchmaking: ActorRef,
            login: String,
            password: String,
            name: String) extends EscalateStrategyActor  {

  val logger = LoggerFactory.getLogger(getClass)
  val log = new Slf4j(logger)
  def logged(r: Receive) = new Logged(r, log, None, None, any ⇒ true)

  var client: ActorRef = null

  override def receive = auth

  def auth: Receive = logged({
    /** from Client */
    case AuthenticateAsAdmin(authenticate) ⇒
      if (authenticate.getLogin == login && authenticate.getPassword == password) {
        client = sender
        client ! AuthenticatedAsAdmin
        context become admin
      } else {
        log.debug("reject")
        sender ! CloseConnection
      }
  })

  def admin: Receive = logged({
    /** from Database */
    case AccountStateResponse(accountId, accountState) ⇒
      sendToClient(accountId, accountState)

    /** from Database */
    case AccountNoExists ⇒

    case C2B.GetAccountState(dto) ⇒
      database ! Database.GetAccountState(dto.getAccountId)

    case C2B.DeleteAccount(accountId) ⇒
      database ! Database.DeleteAccount(accountId)

    case msg: Database.AccountDeleted ⇒
      matchmaking forward msg

    case GetServerHealth ⇒
      matchmaking ! GetServerHealth

    case msg: ServerHealth ⇒
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
            update(accountId, accountState.setBuilding(dto.getSlot.getId, dto.getSlot.getBuildingPrototype))
          else
            update(accountId, accountState.removeBuilding(dto.getSlot.getId))
      )
  })

  def sendToClient(accountId: AccountIdDTO, accountState: AccountStateDTO) =
    client ! B2C.AccountState(
      AdminAccountStateDTO.newBuilder
        .setAccountId(accountId)
        .setAccountState(accountState)
        .build)

  def getState(accountId: AccountIdDTO, f: (AccountIdDTO, AccountState) ⇒ Unit) = {
    val future = Patterns.ask(database, GetAccountState(accountId), 5 seconds)
    val result = Await.result(future, 5 seconds)

    result match {
      case AccountStateResponse(accountId, accountStateDto) ⇒
        f(accountId, AccountState(accountStateDto))

      case invalid ⇒
        log.error("invalid result=" + invalid)
    }
  }

  def update(accountId: AccountIdDTO, newAccountState: AccountState): Unit = {
    val future = Patterns.ask(database, UpdateAccountState(accountId, newAccountState.dto), 5 seconds)
    val result = Await.result(future, 5 seconds)

    result match {
      case AccountStateResponse(accountId, accountStateDto) ⇒
        matchmaking ! AdminSetAccountState(AccountId(accountId), accountStateDto)
        sendToClient(accountId, accountStateDto)

      case invalid ⇒
        log.error("invalid result=" + invalid)
    }
  }
}
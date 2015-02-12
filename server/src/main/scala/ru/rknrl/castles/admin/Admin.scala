package ru.rknrl.castles.admin

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.pattern.Patterns
import ru.rknrl.EscalateStrategyActor
import ru.rknrl.castles.{MatchMaking, AccountId}
import MatchMaking.AdminSetAccountState
import ru.rknrl.castles.AccountId
import ru.rknrl.castles.database.AccountStateDb.{Get, NoExist, StateResponse, Update}
import ru.rknrl.castles.account.state.{AccountState, BuildingPrototype}
import ru.rknrl.castles.rmi._
import ru.rknrl.core.rmi.{ReceiverRegistered, RegisterReceiver}
import ru.rknrl.dto.AccountDTO.AccountStateDTO
import ru.rknrl.dto.AdminDTO.AdminAccountStateDTO
import ru.rknrl.dto.CommonDTO.AccountIdDTO

import scala.concurrent.Await
import scala.concurrent.duration._

class Admin(tcpSender: ActorRef,
            tcpReceiver: ActorRef,
            accountStateDb: ActorRef,
            matchmaking: ActorRef,
            name: String) extends EscalateStrategyActor with ActorLogging {

  private val rmi = context.actorOf(Props(classOf[AdminRMI], tcpSender, self), "admin-rmi" + name)
  tcpReceiver ! RegisterReceiver(rmi)

  def receive = {
    case ReceiverRegistered(ref) ⇒

    /** from AccountStateDb */
    case StateResponse(accountId, accountState) ⇒ sendToClient(accountId, accountState)

    /** from AccountStateDb */
    case NoExist ⇒ log.info("account does not exist")

    case GetAccountStateMsg(dto) ⇒
      accountStateDb ! Get(dto.getAccountId)

    case AddGoldMsg(dto) ⇒
      getState(dto.getAccountId,
        (accountId, accountState) ⇒ update(accountId, accountState.addGold(dto.getAmount))
      )

    case AddItemMsg(dto) ⇒
      getState(dto.getAccountId,
        (accountId, accountState) ⇒ update(accountId, accountState.addItem(dto.getItemType, dto.getAmount))
      )

    case SetSkillMsg(dto) ⇒
      getState(dto.getAccountId,
        (accountId, accountState) ⇒ update(accountId, accountState.setSkill(dto.getSkilType, dto.getSkillLevel))
      )

    case SetSlotMsg(dto) ⇒
      getState(dto.getAccountId,
        (accountId, accountState) ⇒
          if (dto.getSlot.hasBuildingPrototype)
            update(accountId, accountState.setBuilding(dto.getSlot.getId, BuildingPrototype.fromDto(dto.getSlot.getBuildingPrototype)))
          else
            update(accountId, accountState.removeBuilding(dto.getSlot.getId))
      )
  }

  private def sendToClient(accountId: AccountIdDTO, accountState: AccountStateDTO) =
    rmi ! AccountStateMsg(
      AdminAccountStateDTO.newBuilder()
        .setAccountId(accountId)
        .setAccountState(accountState)
        .build())

  private def getState(accountId: AccountIdDTO, f: (AccountIdDTO, AccountState) ⇒ Unit) = {
    val future = Patterns.ask(accountStateDb, Get(accountId), 5 seconds)
    val result = Await.result(future, 5 seconds)

    result match {
      case StateResponse(accountId, accountStateDto) ⇒
        f(accountId, AccountState.fromDto(accountStateDto))

      case invalid ⇒
        log.info("invalid result=" + invalid)
    }
  }

  private def update(accountId: AccountIdDTO, newAccountState: AccountState): Unit = {
    val future = Patterns.ask(accountStateDb, Update(accountId, newAccountState.dto), 5 seconds)
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
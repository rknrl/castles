package ru.rknrl.base

import java.net.InetSocketAddress

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.pattern.Patterns
import ru.rknrl.base.database.AccountStateDb.{Get, NoExist, StateResponse, Update}
import ru.rknrl.castles.account.AccountState
import ru.rknrl.castles.account.objects.BuildingPrototype
import ru.rknrl.castles.rmi._
import ru.rknrl.core.rmi.{ReceiverRegistered, RegisterReceiver, TcpReceiver}
import ru.rknrl.dto.AccountDTO.AccountStateDTO
import ru.rknrl.dto.AdminDTO.AdminAccountStateDTO
import ru.rknrl.dto.CommonDTO.AccountIdDTO
import ru.rknrl.{EscalateStrategyActor, StoppingStrategyActor}

import scala.concurrent.Await
import scala.concurrent.duration._

class AdminTcpServer(tcp: ActorRef, host: String, port: Int, accountStateDb: ActorRef) extends StoppingStrategyActor with ActorLogging {

  import akka.io.Tcp._

  val address = new InetSocketAddress(host, port)

  tcp ! Bind(self, address)

  def receive = {
    case Bound(localAddress) ⇒
      log.info("bound " + localAddress)

    case CommandFailed(_: Bind) ⇒
      log.info("command failed " + address)
      context stop self

    case Connected(remote, local) ⇒
      val name = remote.getAddress.getHostAddress + ":" + remote.getPort
      val tcpSender = sender()
      val tcpReceiver = context.actorOf(Props(classOf[AuthTcpReceiver], tcpSender, accountStateDb, name), "admin-tcp-receiver" + name)
      tcpSender ! Register(tcpReceiver)
  }
}

class AuthTcpReceiver(tcpSender: ActorRef,
                      accountStateDb: ActorRef,
                      name: String) extends TcpReceiver(name) with ActorLogging {

  context.actorOf(Props(classOf[AdminAuth], tcpSender, self, accountStateDb, name), "admin-auth" + name)
}

class AdminAuth(tcpSender: ActorRef, tcpReceiver: ActorRef, accountStateDb: ActorRef, name: String) extends EscalateStrategyActor with ActorLogging {

  private val rmi = context.actorOf(Props(classOf[AdminAuthRMI], tcpSender, self), "admin-auth-rmi" + name)
  tcpReceiver ! RegisterReceiver(rmi)

  def receive = {
    case ReceiverRegistered(ref) ⇒
      rmi ! AdminAuthReadyMsg()

    /** from player */
    case AuthenticateAsAdminMsg(authenticate) ⇒
      context.actorOf(Props(classOf[Admin], tcpSender, tcpReceiver, accountStateDb, name), "admin" + name)
      sender ! AuthenticatedAsAdminMsg()

  }
}

class Admin(tcpSender: ActorRef, tcpReceiver: ActorRef, accountStateDb: ActorRef, name: String) extends EscalateStrategyActor with ActorLogging {

  private val rmi = context.actorOf(Props(classOf[AdminRMI], tcpSender, self), "admin-rmi" + name)
  tcpReceiver ! RegisterReceiver(rmi)

  def receive = {
    case ReceiverRegistered(ref) ⇒

    /** from AccountStateDb */
    case StateResponse(accountId, accountState) ⇒
      rmi ! AccountStateMsg(
        AdminAccountStateDTO.newBuilder()
          .setAccountId(accountId)
          .setAccountState(accountState)
          .build())

    /** from AccountStateDb */
    case NoExist ⇒ log.info("account does not exist")

    case GetAccountStateMsg(dto) ⇒
      accountStateDb ! Get(dto.getAccountId)

    case AddGoldMsg(dto) ⇒
      update(dto.getAccountId,
        (accountId, accountStateDto) ⇒ {
          accountStateDb ! Update(accountId, AccountState.fromDto(accountStateDto).addGold(dto.getAmount).dto)
        })

    case AddItemMsg(dto) ⇒
      update(dto.getAccountId,
        (accountId, accountStateDto) ⇒ {
          accountStateDb ! Update(accountId, AccountState.fromDto(accountStateDto).addItem(dto.getItemType, dto.getAmount).dto)
        })

    case SetSkillMsg(dto) ⇒
      update(dto.getAccountId,
        (accountId, accountStateDto) ⇒ {
          accountStateDb ! Update(accountId, AccountState.fromDto(accountStateDto).setSkill(dto.getSkilType, dto.getSkillLevel).dto)
        })

    case SetSlotMsg(dto) ⇒
      update(dto.getAccountId,
        (accountId, accountStateDto) ⇒ {
          if (dto.getSlot.hasBuildingPrototype)
            accountStateDb ! Update(accountId, AccountState.fromDto(accountStateDto).setBuilding(dto.getSlot.getId, BuildingPrototype.fromDto(dto.getSlot.getBuildingPrototype)).dto)
          else
            accountStateDb ! Update(accountId, AccountState.fromDto(accountStateDto).removeBuilding(dto.getSlot.getId).dto)
        })
  }

  private def update(accountId: AccountIdDTO, f: (AccountIdDTO, AccountStateDTO) ⇒ Unit) = {
    val future = Patterns.ask(accountStateDb, Get(accountId), 5 seconds)
    val result = Await.result(future, 5 seconds)

    result match {
      case StateResponse(accountId, accountStateDto) ⇒
        f(accountId, accountStateDto)

      case invalid ⇒
        log.info("invalid result=" + invalid)
    }
  }
}
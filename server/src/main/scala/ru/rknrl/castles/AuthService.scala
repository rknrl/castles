package ru.rknrl.castles

import akka.actor.{Actor, ActorRef, Props}
import ru.rknrl.castles.account.{Account, AccountState, GetAccountState}
import ru.rknrl.castles.config.Config
import ru.rknrl.castles.database.AccountStateDb.{Get, NoExist}
import ru.rknrl.castles.rmi._
import ru.rknrl.core.rmi.{CloseConnection, ReceiverRegistered, RegisterReceiver, UnregisterReceiver}
import ru.rknrl.core.social.SocialAuth
import ru.rknrl.dto.AccountDTO.AccountStateDTO
import ru.rknrl.dto.AuthDTO.{AuthenticateDTO, AuthenticationSuccessDTO}
import ru.rknrl.dto.CommonDTO.{AccountType, DeviceType}

class AuthService(tcpSender: ActorRef, tcpReceiver: ActorRef,
                  matchmaking: ActorRef,
                  accountStateDb: ActorRef,
                  config: Config,
                  name: String) extends Actor {

  private val authRmi = context.actorOf(Props(classOf[AuthRMI], tcpSender, self), "auth-rmi" + name)

  private var accountId: Option[AccountId] = None
  private var deviceType: Option[DeviceType] = None

  tcpReceiver ! RegisterReceiver(authRmi)

  def checkSecret(authenticate: AuthenticateDTO) =
    authenticate.getUserInfo.getAccountId.getType match {
      case AccountType.DEV ⇒
        true
      case AccountType.VKONTAKTE ⇒
        SocialAuth.checkSecretVk(authenticate.getSecret.getBody, authenticate.getUserInfo.getAccountId.getId, config.social.vk.get)

      case AccountType.ODNOKLASSNIKI ⇒
        SocialAuth.checkSecretOk(authenticate.getSecret.getBody, authenticate.getSecret.getParams, authenticate.getUserInfo.getAccountId.getId, config.social.ok.get)

      case AccountType.MOIMIR ⇒
        SocialAuth.checkSecretMm(authenticate.getSecret.getBody, authenticate.getSecret.getParams, config.social.mm.get)

      case _ ⇒ false
    }

  def reject() = {
    println("reject")
    tcpReceiver ! UnregisterReceiver(authRmi)
    tcpReceiver ! CloseConnection
  }

  def receive = {
    case ReceiverRegistered(ref) ⇒
      authRmi ! AuthReadyMsg()

    /**
     * from player
     */
    case AuthenticateMsg(authenticate) ⇒
      if (checkSecret(authenticate)) {
        accountId = Some(new AccountId(authenticate.getUserInfo.getAccountId))
        deviceType = Some(authenticate.getDeviceType)
        accountStateDb ! Get(accountId.get)
      } else
        reject()

    /**
     * from AccountStateDb
     */
    case dto: AccountStateDTO ⇒
      val state = AccountState.fromDto(dto, config.account)
      startAccount(state)

    /**
     * from AccountStateDb
     */
    case NoExist(key) ⇒
      val state = AccountState.initAccount(config.account)
      startAccount(state)

    /**
     * from Account
     */
    case dto: AuthenticationSuccessDTO ⇒
      authRmi ! AuthenticationSuccessMsg(dto)
      tcpReceiver ! UnregisterReceiver(authRmi)
  }

  private def startAccount(state: AccountState) = {
    val accountRef = context.actorOf(Props(classOf[Account], accountId.get, state, deviceType.get, tcpSender, tcpReceiver, matchmaking, accountStateDb, self, config, name), "account" + name)
    accountRef ! GetAccountState
  }

  override def preStart(): Unit = println("AuthService start " + name)

  override def postStop(): Unit = println("AuthService stop " + name)
}

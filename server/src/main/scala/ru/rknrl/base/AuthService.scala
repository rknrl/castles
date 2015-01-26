package ru.rknrl.base

import akka.actor.{ActorLogging, ActorRef, Props}
import ru.rknrl.EscalateStrategyActor
import ru.rknrl.base.account.Account.GetAuthenticationSuccess
import ru.rknrl.castles.Config
import ru.rknrl.castles.account.{AccountState, CastlesAccount}
import ru.rknrl.castles.database.AccountStateDb.{Insert, Get, NoExist}
import ru.rknrl.castles.rmi._
import ru.rknrl.core.rmi.{CloseConnection, ReceiverRegistered, RegisterReceiver, UnregisterReceiver}
import ru.rknrl.core.social.SocialAuth
import ru.rknrl.dto.AccountDTO.AccountStateDTO
import ru.rknrl.dto.AuthDTO.{AuthenticateDTO, AuthenticationSuccessDTO}
import ru.rknrl.dto.CommonDTO.{AccountType, DeviceType, UserInfoDTO}

class AuthService(tcpSender: ActorRef, tcpReceiver: ActorRef,
                  matchmaking: ActorRef,
                  accountStateDb: ActorRef,
                  config: Config,
                  name: String) extends EscalateStrategyActor with ActorLogging {

  private val authRmi = context.actorOf(Props(classOf[AuthRMI], tcpSender, self), "auth-rmi" + name)

  private var accountId: Option[AccountId] = None
  private var deviceType: Option[DeviceType] = None
  private var userInfo: Option[UserInfoDTO] = None

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
    log.info("reject")
    tcpReceiver ! UnregisterReceiver(authRmi)
    tcpReceiver ! CloseConnection
  }

  def receive = {
    case ReceiverRegistered(ref) ⇒
      authRmi ! AuthReadyMsg()

    /** from player */
    case AuthenticateMsg(authenticate) ⇒
      if (checkSecret(authenticate)) {
        accountId = Some(new AccountId(authenticate.getUserInfo.getAccountId))
        deviceType = Some(authenticate.getDeviceType)
        userInfo = Some(authenticate.getUserInfo)
        accountStateDb ! Get(accountId.get)
      } else
        reject()

    /** from AccountStateDb */
    case NoExist ⇒
      accountStateDb ! Insert(accountId.get, AccountState.initAccount(config.account).dto)

    /** from AccountStateDb */
    case dto: AccountStateDTO ⇒
      val state = AccountState.fromDto(dto, config.account)
      val accountRef = context.actorOf(Props(classOf[CastlesAccount], accountId.get, state, deviceType.get, userInfo.get, tcpSender, tcpReceiver, matchmaking, accountStateDb, self, config, name), "account" + name)
      accountRef ! GetAuthenticationSuccess

    /** from Account */
    case dto: AuthenticationSuccessDTO ⇒
      authRmi ! AuthenticationSuccessMsg(dto)
      tcpReceiver ! UnregisterReceiver(authRmi)
  }

  override def preStart(): Unit = log.info("AuthService start " + name)

  override def postStop(): Unit = log.info("AuthService stop " + name)
}

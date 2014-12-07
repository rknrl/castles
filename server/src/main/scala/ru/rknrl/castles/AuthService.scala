package ru.rknrl.castles

import akka.actor.{Actor, ActorRef, Props}
import ru.rknrl.castles.account.{Account, GetAccountState}
import ru.rknrl.castles.config.Config
import ru.rknrl.castles.rmi.b2c.{AuthRMI, AuthenticateMsg, AuthenticationResultMsg}
import ru.rknrl.core.rmi.{CloseConnection, RegisterReceiver, UnregisterReceiver}
import ru.rknrl.core.social.SocialAuth
import ru.rknrl.dto.AccountDTO.AccountStateDTO
import ru.rknrl.dto.AuthDTO.{AccountType, AuthenticateDTO}

class AuthService(tcpSender: ActorRef, tcpReceiver: ActorRef,
                  matchmaking: ActorRef,
                  config: Config,
                  name: String) extends Actor {

  private val authRmi = context.actorOf(Props(classOf[AuthRMI], tcpSender, self), "auth-rmi" + name)

  tcpReceiver ! RegisterReceiver(authRmi)

  def checkSecret(authenticate: AuthenticateDTO) =
    authenticate.getAccountId.getType match {
      case AccountType.DEV ⇒
        true
      case AccountType.VKONTAKTE ⇒
        SocialAuth.checkSecretVk(authenticate.getSecret.getBody, authenticate.getAccountId.getId, config.social.vk.get)

      case AccountType.ODNOKLASSNIKI ⇒
        SocialAuth.checkSecretOk(authenticate.getSecret.getBody, authenticate.getSecret.getParams, authenticate.getAccountId.getId, config.social.ok.get)

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
    /**
     * from player
     */
    case AuthenticateMsg(authenticate) ⇒
      if (checkSecret(authenticate)) {
        val accountRef = context.actorOf(Props(classOf[Account], new AccountId(authenticate.getAccountId), authenticate.getDeviceType, tcpSender, tcpReceiver, matchmaking, self, config, name), "account" + name)
        accountRef ! GetAccountState
      } else
        reject()

    /**
     * from Account
     */
    case dto: AccountStateDTO ⇒
      authRmi ! AuthenticationResultMsg(dto)
      tcpReceiver ! UnregisterReceiver(authRmi)
  }

  override def preStart(): Unit = println("AuthService start " + name)

  override def postStop(): Unit = println("AuthService stop " + name)
}

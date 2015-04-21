//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account

import akka.actor.{Actor, ActorRef}
import ru.rknrl.Assertion
import ru.rknrl.castles.Config
import ru.rknrl.castles.account.NewAccount.ClientInfo
import ru.rknrl.castles.account.auth.Auth.SecretChecked
import ru.rknrl.castles.database.Database._
import ru.rknrl.castles.matchmaking.NewMatchmaking._
import ru.rknrl.castles.rmi.B2C.Authenticated
import ru.rknrl.core.rmi.CloseConnection
import ru.rknrl.dto._


object NewAccount {

  sealed case class ClientInfo(ref: ActorRef,
                               accountId: AccountId,
                               deviceType: DeviceType,
                               platformType: PlatformType,
                               userInfo: UserInfoDTO)

}

class NewAccount(matchmaking: ActorRef,
                 auth: ActorRef,
                 database: ActorRef,
                 config: Config) extends Actor {

  var _client: Option[ClientInfo] = None
  var _state: Option[AccountState] = None
  var _tutorState: Option[TutorStateDTO] = None

  def client = _client.get

  def state = _state.get

  def tutorState = _tutorState.get

  def receive = {
    case authenticate@AuthenticateDTO(userInfo, platformType, deviceType, secret) ⇒
      _client = Some(ClientInfo(sender, userInfo.accountId, deviceType, platformType, userInfo))
      auth ! authenticate

    case SecretChecked(valid) ⇒
      if (valid) {
        database ! GetAccountState(client.accountId)
        database ! StatAction.AUTHENTICATED
      } else {
        //log.info("reject")
        client.ref ! CloseConnection
        database ! StatAction.NOT_AUTHENTICATED
      }

    case AccountNoExists ⇒
      val initAccountState = config.account.initAccount
      val initTutorState = TutorStateDTO()
      database ! Insert(client.accountId, initAccountState.dto, client.userInfo, initTutorState)

    case AccountStateResponse(accountId, stateDto) ⇒
      Assertion.check(accountId == client.accountId)
      _state = Some(AccountState(stateDto))
      database ! GetTutorState(client.accountId)

    case TutorStateResponse(accountId, tutorState) ⇒
      Assertion.check(accountId == client.accountId)
      _tutorState = Some(tutorState)
      matchmaking ! Online(client.accountId)
      matchmaking ! InGame(client.accountId)

    case InGameResponse(gameRef, searchOpponents, top) ⇒
      client.ref ! Authenticated(AuthenticatedDTO(
        state.dto,
        config.account.dto,
        TopDTO(top),
        config.productsDto(client.platformType, client.accountId.accountType),
        tutorState,
        searchOpponents,
        game = if (gameRef.isDefined) Some(NodeLocator(config.host, config.gamePort)) else None
      ))

    case DuplicateAccount ⇒ context stop self
  }

  override def postStop(): Unit =
    if (_client.isDefined) matchmaking ! Offline(client.accountId, client.ref)
}

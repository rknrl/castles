//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account

import akka.actor.{Actor, ActorRef}
import ru.rknrl.castles.Config
import ru.rknrl.castles.account.NewAccount.ClientInfo
import ru.rknrl.castles.account.SecretChecker.SecretChecked
import ru.rknrl.castles.database.Database.{GetAccountState, _}
import ru.rknrl.castles.database.Statistics
import ru.rknrl.castles.matchmaking.NewMatchmaking._
import ru.rknrl.castles.rmi.B2C.{AccountStateUpdated, Authenticated}
import ru.rknrl.castles.rmi.C2B._
import ru.rknrl.core.rmi.CloseConnection
import ru.rknrl.dto._
import ru.rknrl.{Assertion, BugType, Logged, SilentLog}


object NewAccount {

  sealed case class ClientInfo(ref: ActorRef,
                               accountId: AccountId,
                               deviceType: DeviceType,
                               platformType: PlatformType,
                               userInfo: UserInfoDTO)

}

class NewAccount(matchmaking: ActorRef,
                 secretChecker: ActorRef,
                 database: ActorRef,
                 bugs: ActorRef,
                 config: Config) extends Actor {

  var _client: Option[ClientInfo] = None
  var _state: Option[AccountState] = None
  var _tutorState: Option[TutorStateDTO] = None

  def client = _client.get

  def state = _state.get

  def tutorState = _tutorState.get

  val log = new SilentLog

  def logged(r: Receive) = new Logged(r, log, Some(bugs), Some(BugType.ACCOUNT), any ⇒ true)

  def receive = auth

  def auth = logged({
    case authenticate@AuthenticateDTO(userInfo, platformType, deviceType, secret) ⇒
      _client = Some(ClientInfo(sender, userInfo.accountId, deviceType, platformType, userInfo))
      secretChecker ! authenticate

    case SecretChecked(valid) ⇒
      if (valid) {
        database ! GetAccountState(client.accountId)
        database ! StatAction.AUTHENTICATED
      } else {
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
      context become enterAccount
  })

  def enterAccount = logged({
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

      if (searchOpponents)
        context become enterGame
      else if (gameRef.isDefined)
        context become game
      else
        context become account

  }).orElse(persistent)

  def account = logged({
    case BuyBuilding(dto) ⇒
      updateState(state.buyBuilding(dto.id, dto.buildingType, config.account))
      database ! Statistics.buyBuilding(dto.buildingType, BuildingLevel.LEVEL_1)

    case UpgradeBuilding(dto) ⇒
      updateState(state.upgradeBuilding(dto.id, config.account))
      database ! Statistics.buyBuilding(state.slots(dto.id).get)

    case RemoveBuilding(dto) ⇒
      updateState(state.removeBuilding(dto.id))
      database ! StatAction.REMOVE_BUILDING

    case UpgradeSkill(dto) ⇒
      updateState(state.upgradeSkill(dto.skillType, config.account))
      database ! Statistics.buySkill(dto.skillType, state.skills(dto.skillType))

    case BuyItem(dto) ⇒
      updateState(state.buyItem(dto.itemType, config.account))
      database ! Statistics.buyItem(dto.itemType)

    case EnterGame ⇒
    //      matchmaking ! GameOrder(client.accountId, client.deviceType, client.userInfo, state, isBot = false)
    //      context become enterGame

  }).orElse(persistent)

  def updateState(newState: AccountState): Unit = {
    _state = Some(newState)
    database ! UpdateAccountState(client.accountId, newState.dto)
  }

  def enterGame = logged({
    case "a" ⇒
  }).orElse(persistent)

  def game = logged({
    case "a" ⇒
  }).orElse(persistent)

  def persistent = logged({
    case AccountStateResponse(accountId, stateDto) ⇒
      client.ref ! AccountStateUpdated(stateDto)

    case DuplicateAccount ⇒ context stop self
  })

  override def postStop(): Unit =
    if (_client.isDefined) matchmaking ! Offline(client.accountId, client.ref)
}

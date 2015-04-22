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
import ru.rknrl.castles.account.Account.ClientInfo
import ru.rknrl.castles.account.SecretChecker.SecretChecked
import ru.rknrl.castles.database.Database.{GetAccountState, _}
import ru.rknrl.castles.database.{Database, Statistics}
import ru.rknrl.castles.game.Game.Join
import ru.rknrl.castles.matchmaking.NewMatchmaking._
import ru.rknrl.castles.rmi.B2C.{AccountStateUpdated, Authenticated, EnteredGame}
import ru.rknrl.castles.rmi.C2B._
import ru.rknrl.castles.rmi.{B2C, C2B}
import ru.rknrl.core.rmi.CloseConnection
import ru.rknrl.dto._
import ru.rknrl.{Assertion, BugType, Logged, SilentLog}


object Account {

  sealed case class ClientInfo(ref: ActorRef,
                               accountId: AccountId,
                               deviceType: DeviceType,
                               platformType: PlatformType,
                               userInfo: UserInfoDTO)

}

class Account(matchmaking: ActorRef,
              secretChecker: ActorRef,
              database: ActorRef,
              bugs: ActorRef,
              config: Config) extends Actor {

  var _client: Option[ClientInfo] = None
  var _state: Option[AccountState] = None
  var _tutorState: Option[TutorStateDTO] = None
  var _game: Option[ActorRef] = None

  def client = _client.get

  def state = _state.get

  def tutorState = _tutorState.get

  def game = _game.get

  val log = new SilentLog

  def logged(r: Receive) = new Logged(r, log, Some(bugs), Some(BugType.ACCOUNT), any ⇒ true)

  def receive = auth

  def auth: Receive = logged({
    case Authenticate(authenticate) ⇒
      _client = Some(ClientInfo(sender, authenticate.userInfo.accountId, authenticate.deviceType, authenticate.platformType, authenticate.userInfo))
      secretChecker ! authenticate

    case SecretChecked(valid) ⇒
      if (valid) {
        database ! GetAccountState(client.accountId)
        database ! StatAction.AUTHENTICATED
      } else {
        database ! StatAction.NOT_AUTHENTICATED
        client.ref ! CloseConnection
      }

    case AccountNoExists ⇒
      val initAccountState = config.account.initAccount
      val initTutorState = TutorStateDTO()
      database ! Insert(client.accountId, initAccountState.dto, client.userInfo, initTutorState)
      database ! StatAction.FIRST_AUTHENTICATED

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

  def enterAccount: Receive = logged({
    case InGameResponse(gameRef, searchOpponents, top) ⇒

      client.ref ! Authenticated(AuthenticatedDTO(
        state.dto,
        config.account.dto,
        TopDTO(top),
        config.productsDto(client.platformType, client.accountId.accountType),
        tutorState,
        searchOpponents,
        game = if (gameRef.isDefined) Some(nodeLocator) else None
      ))

      if (searchOpponents)
        context become enterGame
      else if (gameRef.isDefined)
        context become inGame
      else if (state.gamesCount == 0) {
        database ! StatAction.START_TUTOR
        sendGameOrder()
      } else
        context become account

  }).orElse(persistent)

  def account: Receive = logged({
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

    case EnterGame ⇒ sendGameOrder()

  }).orElse(persistent)

  def enterGame: Receive = logged({
    case ConnectToGame(gameRef) ⇒
      _game = Some(gameRef)
      client.ref ! EnteredGame(nodeLocator)

    case C2B.JoinGame ⇒
      game ! Join(client.accountId, client.ref)
      context become inGame

  }).orElse(persistent)

  def inGame: Receive = logged({
    case msg: GameMsg ⇒ game forward msg

    case msg: UpdateStatistics ⇒
      game forward msg.stat.action
      database forward msg.stat.action

    case AccountLeaveGame(top) ⇒
      client.ref ! B2C.LeavedGame
      client.ref ! B2C.TopUpdated(TopDTO(top))
      context become account

  }).orElse(persistent)

  def persistent: Receive = logged({
    case AccountStateResponse(accountId, stateDto) ⇒
      client.ref ! AccountStateUpdated(stateDto)

    case SetAccountState(_, accountStateDto) ⇒
      _state = Some(AccountState(accountStateDto))
      client.ref ! AccountStateUpdated(accountStateDto)

    case msg: UpdateStatistics ⇒ database ! msg.stat.action

    case C2B.UpdateTutorState(state) ⇒
      database ! Database.UpdateTutorState(client.accountId, state)

    case DuplicateAccount ⇒ client.ref ! CloseConnection
  })

  def updateState(newState: AccountState): Unit = {
    _state = Some(newState)
    database ! UpdateAccountState(client.accountId, newState.dto)
  }

  def sendGameOrder(): Unit = {
    matchmaking ! GameOrder(client.accountId, client.deviceType, client.userInfo, state, isBot = false)
    context become enterGame
  }

  def nodeLocator = NodeLocator(config.host, config.gamePort)

  override def postStop(): Unit =
    if (_client.isDefined) matchmaking ! Offline(client.accountId, client.ref)
}

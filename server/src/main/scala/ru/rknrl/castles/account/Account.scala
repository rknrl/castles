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
import ru.rknrl.castles.account.Account.ClientInfo
import ru.rknrl.castles.account.SecretChecker.SecretChecked
import ru.rknrl.castles.database.Database.{GetAccountState, _}
import ru.rknrl.castles.database.{Database, Statistics}
import ru.rknrl.castles.game.Game.Join
import ru.rknrl.castles.matchmaking.MatchMaking._
import ru.rknrl.castles.rmi.B2C.{AccountStateUpdated, Authenticated, EnteredGame}
import ru.rknrl.castles.rmi.C2B._
import ru.rknrl.castles.rmi.{B2C, C2B}
import ru.rknrl.core.rmi.CloseConnection
import ru.rknrl.dto._
import ru.rknrl.logging.ActorLog


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
              graphite: ActorRef,
              config: Config) extends Actor with ActorLog {

  var _client: Option[ClientInfo] = None
  var _state: Option[AccountState] = None
  var _tutorState: Option[TutorStateDTO] = None
  var _game: Option[ActorRef] = None

  def client = _client.get

  def state = _state.get

  def tutorState = _tutorState.get

  def game = _game.get

  def receive = auth

  def auth: Receive = logged {
    case Authenticate(authenticate) ⇒
      _client = Some(ClientInfo(sender, authenticate.userInfo.accountId, authenticate.deviceType, authenticate.platformType, authenticate.userInfo))
      send(secretChecker, authenticate)

    case SecretChecked(valid) ⇒
      if (valid) {
        send(database, GetAccountState(client.accountId))
        send(graphite, StatAction.AUTHENTICATED)
      } else {
        send(graphite, StatAction.NOT_AUTHENTICATED)
        send(client.ref, CloseConnection)
      }

    case AccountNoExists ⇒
      val initAccountState = config.account.initAccount
      val initTutorState = TutorStateDTO()
      send(database, Insert(client.accountId, initAccountState.dto, client.userInfo, initTutorState, initAccountState.rating))
      send(graphite, StatAction.FIRST_AUTHENTICATED)

    case AccountStateResponse(accountId, stateDto, ratingOption) ⇒
      Assertion.check(accountId == client.accountId)
      val rating = ratingOption.getOrElse(config.account.initRating)
      _state = Some(AccountState.fromDto(stateDto, rating))
      send(database, GetTutorState(client.accountId))

    case TutorStateResponse(accountId, tutorState) ⇒
      Assertion.check(accountId == client.accountId)
      _tutorState = Some(tutorState)
      send(matchmaking, Online(client.accountId))
      send(matchmaking, InGame(client.accountId))
      become(enterAccount, "enterAccount")
  }

  def enterAccount: Receive = logged {
    case InGameResponse(gameRef, searchOpponents, top) ⇒

      val isTutor = state.gamesCount == 0

      send(client.ref, Authenticated(AuthenticatedDTO(
        state.dto,
        config.account.dto,
        TopDTO(top),
        config.productsDto(client.platformType, client.accountId.accountType),
        tutorState,
        searchOpponents || isTutor,
        game = if (gameRef.isDefined) Some(nodeLocator) else None
      )))

      if (searchOpponents)
        become(enterGame, "enterGame")
      else if (gameRef.isDefined) {
        _game = gameRef
        become(enterGame, "enterGame")
      } else if (isTutor) {
        send(graphite, StatAction.START_TUTOR)
        sendGameOrder()
      } else
        become(account, "account")

  }.orElse(persistent)

  def account: Receive = logged {
    case BuyBuilding(dto) ⇒
      updateState(state.buyBuilding(dto.id, dto.buildingType, config.account))
      send(graphite, Statistics.buyBuilding(dto.buildingType, BuildingLevel.LEVEL_1))

    case UpgradeBuilding(dto) ⇒
      updateState(state.upgradeBuilding(dto.id, config.account))
      send(graphite, Statistics.buyBuilding(state.slots(dto.id).get))

    case RemoveBuilding(dto) ⇒
      updateState(state.removeBuilding(dto.id))
      send(graphite, StatAction.REMOVE_BUILDING)

    case UpgradeSkill(dto) ⇒
      updateState(state.upgradeSkill(dto.skillType, config.account))
      send(graphite, Statistics.buySkill(dto.skillType, state.skills(dto.skillType)))

    case BuyItem(dto) ⇒
      updateState(state.buyItem(dto.itemType, config.account))
      send(graphite, Statistics.buyItem(dto.itemType))

    case EnterGame ⇒ sendGameOrder()

  }.orElse(persistent)

  def enterGame: Receive = logged {
    case ConnectToGame(gameRef) ⇒
      _game = Some(gameRef)
      send(client.ref, EnteredGame(nodeLocator))

    case C2B.JoinGame ⇒
      send(game, Join(client.accountId, client.ref))
      become(inGame, "inGame")

  }.orElse(persistent)

  def inGame: Receive = logged {
    case msg: GameMsg ⇒ forward(game, msg)

    case msg: UpdateStatistics ⇒
      forward(game, msg.stat.action)
      forward(graphite, msg.stat.action)

    case AccountLeaveGame(top) ⇒
      send(client.ref, B2C.LeavedGame)
      send(client.ref, B2C.TopUpdated(TopDTO(top)))
      become(account, "account")

  }.orElse(persistent)

  def persistent: Receive = logged {
    case AccountStateResponse(accountId, stateDto, rating) ⇒
      send(client.ref, AccountStateUpdated(stateDto))

    case SetAccountState(_, accountStateDto, rating) ⇒
      _state = Some(AccountState.fromDto(accountStateDto, rating))
      send(client.ref, AccountStateUpdated(accountStateDto))

    case msg: UpdateStatistics ⇒ send(graphite, msg.stat.action)

    case C2B.UpdateTutorState(state) ⇒
      send(database, Database.UpdateTutorState(client.accountId, state))

    case DuplicateAccount ⇒ send(client.ref, CloseConnection)
  }

  def updateState(newState: AccountState): Unit = {
    _state = Some(newState)
    send(database, UpdateAccountState(client.accountId, newState.dto, newState.rating))
  }

  def sendGameOrder(): Unit = {
    send(matchmaking, GameOrder(client.accountId, client.deviceType, client.userInfo, state, isBot = false))
    become(enterGame, "enterGame")
  }

  def nodeLocator = NodeLocator(config.host, config.gamePort)

  override def postStop(): Unit =
    if (_client.isDefined) send(matchmaking, Offline(client.accountId, client.ref))
}

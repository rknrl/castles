//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account

import akka.actor.{Actor, ActorRef, Props}
import ru.rknrl.castles.Config
import ru.rknrl.castles.account.Account.ClientInfo
import ru.rknrl.castles.account.SecretChecker.SecretChecked
import ru.rknrl.castles.database.Database._
import ru.rknrl.castles.database.{Database, Statistics}
import ru.rknrl.castles.game.Game.Join
import ru.rknrl.castles.matchmaking.MatchMaking._
import ru.rknrl.castles.rmi.B2C.{AccountStateUpdated, Authenticated, EnteredGame, PlaceUpdated}
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

  def props(matchmaking: ActorRef,
            secretChecker: ActorRef,
            databaseQueue: ActorRef,
            graphite: ActorRef,
            config: Config) =
    Props(classOf[Account], matchmaking, secretChecker, databaseQueue, graphite, config)

}

class Account(matchmaking: ActorRef,
              secretChecker: ActorRef,
              databaseQueue: ActorRef,
              graphite: ActorRef,
              config: Config) extends Actor with ActorLog {

  var _client: Option[ClientInfo] = None
  var _game: Option[ActorRef] = None

  def client = _client.get

  def game = _game.get

  def receive = auth

  def auth: Receive = logged {
    case Authenticate(authenticate) ⇒
      _client = Some(ClientInfo(sender, authenticate.userInfo.accountId, authenticate.deviceType, authenticate.platformType, authenticate.userInfo))
      send(secretChecker, authenticate)

    case SecretChecked(valid) ⇒
      if (valid) {
        send(databaseQueue, GetAccount(client.accountId))
        send(databaseQueue, UpdateUserInfo(client.accountId, client.userInfo))
        send(graphite, StatAction.AUTHENTICATED)
      } else {
        send(graphite, StatAction.NOT_AUTHENTICATED)
        send(client.ref, CloseConnection)
      }

    case AccountResponse(accountId, stateDto, ratingDto, tutorStateDto, place) ⇒
      val state = stateDto.getOrElse(config.account.initState)
      val rating = ratingDto.getOrElse(config.account.initRating)
      val tutorState = tutorStateDto.getOrElse(TutorStateDTO())

      if (stateDto.isEmpty) send(graphite, StatAction.FIRST_AUTHENTICATED)

      send(matchmaking, Online(client.accountId))
      send(matchmaking, InGame(client.accountId))
      become(enterAccount(state, rating, tutorState, place), "enterAccount")
  }

  def enterAccount(state: AccountStateDTO,
                   rating: Double,
                   tutorState: TutorStateDTO,
                   place: Long): Receive = logged {
    case InGameResponse(gameRef, searchOpponents, top) ⇒

      val isTutor = state.gamesCount == 0

      send(client.ref, Authenticated(AuthenticatedDTO(
        state,
        config.account.dto,
        TopDTO(top),
        PlaceDTO(place),
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
      val transform = (stateDto: Option[AccountStateDTO]) ⇒ {
        val state = stateDto.getOrElse(config.account.initState)
        AccountState.buyBuilding(state, dto.id, dto.buildingType, config.account)
      }
      send(databaseQueue, GetAndUpdateAccountState(client.accountId, transform))
      send(graphite, Statistics.buyBuilding(dto.buildingType, BuildingLevel.LEVEL_1))

    case UpgradeBuilding(dto) ⇒
      val transform = (stateDto: Option[AccountStateDTO]) ⇒ {
        val state = stateDto.getOrElse(config.account.initState)
        val newState = AccountState.upgradeBuilding(state, dto.id, config.account)
        send(graphite, Statistics.buyBuilding(newState.slots.find(_.id == dto.id).get.buildingPrototype.get)) // todo
        newState
      }
      send(databaseQueue, GetAndUpdateAccountState(client.accountId, transform))

    case RemoveBuilding(dto) ⇒
      val transform = (stateDto: Option[AccountStateDTO]) ⇒ {
        val state = stateDto.getOrElse(config.account.initState)
        AccountState.removeBuilding(state, dto.id)
      }
      send(databaseQueue, GetAndUpdateAccountState(client.accountId, transform))
      send(graphite, StatAction.REMOVE_BUILDING)

    case UpgradeSkill(dto) ⇒
      val transform = (stateDto: Option[AccountStateDTO]) ⇒ {
        val state = stateDto.getOrElse(config.account.initState)
        val newState = AccountState.upgradeSkill(state, dto.skillType, config.account)
        send(graphite, Statistics.buySkill(dto.skillType, newState.skills.find(_.skillType == dto.skillType).get.level)) // todo
        newState
      }
      send(databaseQueue, GetAndUpdateAccountState(client.accountId, transform))

    case BuyItem(dto) ⇒
      val transform = (stateDto: Option[AccountStateDTO]) ⇒ {
        val state = stateDto.getOrElse(config.account.initState)
        AccountState.buyItem(state, dto.itemType, config.account)
      }
      send(databaseQueue, GetAndUpdateAccountState(client.accountId, transform))
      send(graphite, Statistics.buyItem(dto.itemType))

    case EnterGame ⇒ sendGameOrder()

  }.orElse(persistent)

  def enterGame: Receive = logged {
    case msg: AccountResponse ⇒
      val state = msg.state.getOrElse(config.account.initState)
      val rating = msg.rating.getOrElse(config.account.initRating)
      send(matchmaking, GameOrder(client.accountId, client.deviceType, client.userInfo, state, rating, isBot = false))

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
    case AccountStateResponse(accountId, stateDto) ⇒
      send(client.ref, AccountStateUpdated(stateDto))

    case SetRating(_, newRating, newPlace) ⇒
      send(client.ref, PlaceUpdated(PlaceDTO(newPlace)))

    case SetAccountState(_, accountStateDto) ⇒
      send(client.ref, AccountStateUpdated(accountStateDto))

    case msg: UpdateStatistics ⇒ send(graphite, msg.stat.action)

    case C2B.UpdateTutorState(state) ⇒
      send(databaseQueue, Database.UpdateTutorState(client.accountId, state))

    case DuplicateAccount ⇒ send(client.ref, CloseConnection)
  }

  def sendGameOrder(): Unit = {
    send(databaseQueue, GetAccount(client.accountId))
    become(enterGame, "enterGame")
  }

  def nodeLocator = NodeLocator(config.host, config.gamePort)

  override def postStop(): Unit =
    if (_client.isDefined) send(matchmaking, Offline(client.accountId, client.ref))
}

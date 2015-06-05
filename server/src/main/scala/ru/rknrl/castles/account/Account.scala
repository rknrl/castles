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
import ru.rknrl.castles.account.AccountState._
import ru.rknrl.castles.account.SecretChecker.SecretChecked
import ru.rknrl.castles.database.Database.UpdateUserInfo
import ru.rknrl.castles.database.DatabaseTransaction._
import ru.rknrl.castles.database.{Database, Statistics}
import ru.rknrl.castles.game.Game.Join
import ru.rknrl.castles.matchmaking.MatchMaking._
import ru.rknrl.castles.matchmaking.Top
import ru.rknrl.castles.rmi.B2C._
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
            config: Config,
            calendar: Calendar) =
    Props(classOf[Account], matchmaking, secretChecker, databaseQueue, graphite, config, calendar)

}

class Account(matchmaking: ActorRef,
              secretChecker: ActorRef,
              databaseQueue: ActorRef,
              graphite: ActorRef,
              config: Config,
              calendar: Calendar) extends Actor with ActorLog {

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

    case AccountResponse(accountId, stateDto, ratingDto, tutorStateDto, top, place, lastWeekPlace, lastWeekTop) ⇒
      val state = stateDto.getOrElse(config.account.initState)
      val rating = ratingDto.getOrElse(config.account.initRating)
      val tutorState = tutorStateDto.getOrElse(TutorStateDTO())

      if (stateDto.isEmpty) send(graphite, StatAction.FIRST_AUTHENTICATED)

      send(matchmaking, Online(client.accountId))
      send(matchmaking, InGame(client.accountId))
      become(enterAccount(state, rating, tutorState, top, place, lastWeekPlace, lastWeekTop), "enterAccount")
  }

  def enterAccount(state: AccountStateDTO,
                   rating: Double,
                   tutorState: TutorStateDTO,
                   top: Top,
                   place: Option[Long],
                   lastWeekPlace: Option[Long],
                   lastWeekTop: Top): Receive = logged {
    case InGameResponse(gameRef, searchOpponents) ⇒

      val isTutor = state.gamesCount == 0

      val needSendLastWeek = state.weekNumberAccepted.isEmpty || state.weekNumberAccepted.get < lastWeekTop.weekNumber
      val lastWeekTopDto = if (needSendLastWeek) Some(lastWeekTop.dto) else None
      val placeDto = if (place.isDefined) Some(PlaceDTO(place.get)) else None
      val placeLastWeekDto = if (needSendLastWeek && lastWeekPlace.isDefined) Some(PlaceDTO(lastWeekPlace.get)) else None

      send(client.ref, Authenticated(AuthenticatedDTO(
        state,
        config.account.dto,
        top.dto,
        placeDto,
        config.productsDto(client.platformType, client.accountId.accountType),
        tutorState,
        searchOpponents || isTutor,
        game = if (gameRef.isDefined) Some(nodeLocator) else None,
        lastWeekTopDto,
        placeLastWeekDto
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
    case AcceptPresent ⇒
      getAndUpdate(state ⇒ acceptPresent(state, config.account, calendar))

    case AcceptAdvert(accept) ⇒
      getAndUpdate(state ⇒ acceptAdvert(state, accept.accepted, config.account))

    case AcceptWeekTop(weekNumber) ⇒
      getAndUpdate(state ⇒ acceptWeekTop(state, config.account, weekNumber.weekNumber))

    case BuyBuilding(dto) ⇒
      getAndUpdate(state ⇒ AccountState.buyBuilding(state, dto.id, dto.buildingType, config.account))
      send(graphite, Statistics.buyBuilding(dto.buildingType, BuildingLevel.LEVEL_1))

    case UpgradeBuilding(dto) ⇒
      val transform = (state: Option[AccountStateDTO]) ⇒ {
        val newState = AccountState.upgradeBuilding(state, dto.id, config.account)
        send(graphite, Statistics.buyBuilding(newState.slots.find(_.id == dto.id).get.buildingPrototype.get)) // todo
        newState
      }
      getAndUpdate(transform)

    case RemoveBuilding(dto) ⇒
      getAndUpdate(state ⇒ removeBuilding(state, dto.id, config.account))
      send(graphite, StatAction.REMOVE_BUILDING)

    case UpgradeSkill(dto) ⇒
      val transform = (state: Option[AccountStateDTO]) ⇒ {
        val newState = AccountState.upgradeSkill(state, dto.skillType, config.account)
        send(graphite, Statistics.buySkill(dto.skillType, newState.skills.find(_.skillType == dto.skillType).get.level)) // todo
        newState
      }
      getAndUpdate(transform)

    case BuyItem(dto) ⇒
      getAndUpdate(state ⇒ AccountState.buyItem(state, dto.itemType, config.account))
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

    case AccountLeaveGame ⇒
      send(client.ref, B2C.LeavedGame)
      become(account, "account")

  }.orElse(persistent)

  def persistent: Receive = logged {
    case AccountStateResponse(accountId, state) ⇒
      send(client.ref, AccountStateUpdated(state))

    case AccountStateAndRatingResponse(accountId, state, newRating, newPlace, top) ⇒
      send(client.ref, AccountStateUpdated(state))
      send(client.ref, PlaceUpdated(PlaceDTO(newPlace)))
      send(client.ref, TopUpdated(top.dto))

    case msg: UpdateStatistics ⇒ send(graphite, msg.stat.action)

    case C2B.UpdateTutorState(state) ⇒
      send(databaseQueue, Database.UpdateTutorState(client.accountId, state))

    case DuplicateAccount ⇒ send(client.ref, CloseConnection)
  }

  def getAndUpdate(transform: Option[AccountStateDTO] ⇒ AccountStateDTO): Unit =
    send(databaseQueue, GetAndUpdateAccountState(client.accountId, transform))

  def sendGameOrder(): Unit = {
    send(databaseQueue, GetAccount(client.accountId)) // todo нужно только accountState и rating, не надо запрашивать весь account
    become(enterGame, "enterGame")
  }

  def nodeLocator = NodeLocator(config.host, config.gamePort)

  override def postStop(): Unit =
    if (_client.isDefined) send(matchmaking, Offline(client.accountId, client.ref))
}

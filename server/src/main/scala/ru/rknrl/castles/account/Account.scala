//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import protos.StatAction._
import protos._
import ru.rknrl.castles.Config
import ru.rknrl.castles.account.Account.ClientInfo
import ru.rknrl.castles.account.AccountState._
import ru.rknrl.castles.account.SecretChecker.SecretChecked
import ru.rknrl.castles.game.Game.Join
import ru.rknrl.castles.game.GameMsg
import ru.rknrl.castles.matchmaking.MatchMaking._
import ru.rknrl.castles.matchmaking.Top
import ru.rknrl.castles.storage.Storage.{ReplaceUserInfo, _}
import ru.rknrl.castles.storage.{Calendar, Statistics, Storage}
import ru.rknrl.logging.ShortActorLogging

object Account {

  case class ClientInfo(ref: ActorRef,
                        accountId: AccountId,
                        deviceType: DeviceType,
                        platformType: PlatformType,
                        userInfo: UserInfo)

  def props(matchmaking: ActorRef,
            secretChecker: ActorRef,
            storage: ActorRef,
            graphite: ActorRef,
            config: Config,
            calendar: Calendar) =
    Props(classOf[Account], matchmaking, secretChecker, storage, graphite, config, calendar)

}

class Account(matchmaking: ActorRef,
              secretChecker: ActorRef,
              storage: ActorRef,
              graphite: ActorRef,
              config: Config,
              calendar: Calendar) extends Actor with ShortActorLogging {

  var _client: Option[ClientInfo] = None
  var _game: Option[ActorRef] = None

  def client = _client.get

  def game = _game.get

  def receive = auth

  def auth: Receive = logged {
    case authenticate: Authenticate ⇒
      _client = Some(ClientInfo(sender, authenticate.userInfo.accountId, authenticate.deviceType, authenticate.platformType, authenticate.userInfo))
      send(secretChecker, authenticate)

    case SecretChecked(valid) ⇒
      if (valid) {
        send(storage, GetAccount(client.accountId))
        send(storage, ReplaceUserInfo(client.accountId, client.userInfo))
        sendStat(AUTHENTICATED)
      } else {
        sendStat(NOT_AUTHENTICATED)
        send(client.ref, PoisonPill)
      }

    case AccountResponse(accountId, state, rating, tutorState, top, place, lastWeekTop, lastWeekPlace) ⇒
      if (state.isEmpty) sendStat(FIRST_AUTHENTICATED)
      send(matchmaking, Online(client.accountId))
      send(matchmaking, InGame(client.accountId))

      become(enterAccount(
        state = state getOrElse config.account.initState,
        rating = rating getOrElse config.account.initRating,
        tutorState = tutorState getOrElse TutorState(),
        top,
        place,
        lastWeekTop,
        lastWeekPlace
      ), "enterAccount")
  }

  def enterAccount(state: AccountState,
                   rating: Double,
                   tutorState: TutorState,
                   top: Top,
                   place: Option[Long],
                   lastWeekTop: Top,
                   lastWeekPlace: Option[Long]): Receive = logged(persistent orElse {

    case InGameResponse(gameRef, searchOpponents) ⇒

      val isTutor = state.gamesCount == 0

      val needSendLastWeek = state.weekNumberAccepted.isEmpty || state.weekNumberAccepted.get < lastWeekTop.weekNumber
      val lastWeekTopDto = if (needSendLastWeek) Some(lastWeekTop.dto) else None
      val placeDto = if (place.isDefined) Some(Place(place.get)) else None
      val placeLastWeekDto = if (needSendLastWeek && lastWeekPlace.isDefined) Some(Place(lastWeekPlace.get)) else None

      send(client.ref, Authenticated(
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
      ))

      if (searchOpponents)
        become(enterGame, "enterGame")
      else if (gameRef.isDefined) {
        _game = gameRef
        become(enterGame, "enterGame")
      } else if (isTutor) {
        sendStat(START_TUTOR)
        sendGameOrder()
      } else
        become(account, "account")
  })

  def account: Receive = logged(persistent orElse {
    case AcceptPresent() ⇒
      getAndUpdate(state ⇒ acceptPresent(state, config.account, calendar))

    case AcceptAdvert(accepted) ⇒
      getAndUpdate(state ⇒ acceptAdvert(state, accepted, config.account))

    case AcceptWeekTop(weekNumber) ⇒
      getAndUpdate(state ⇒ acceptWeekTop(state, config.account, weekNumber.weekNumber))

    case buy: BuyBuilding ⇒
      getAndUpdate(state ⇒ buyBuilding(state, buy.id, buy.buildingType, config.account))
      sendStat(Statistics.buyBuilding(buy.buildingType, BuildingLevel.LEVEL_1))

    case upgrade: UpgradeBuilding ⇒
      val transform = (state: Option[AccountState]) ⇒ {
        val newState = upgradeBuilding(state, upgrade.id, config.account)
        sendStat(Statistics.buyBuilding(newState.slots.find(_.id == upgrade.id).get.buildingPrototype.get)) // todo: send stat in closure
        newState
      }
      getAndUpdate(transform)

    case remove: RemoveBuilding ⇒
      getAndUpdate(state ⇒ removeBuilding(state, remove.id, config.account))
      sendStat(REMOVE_BUILDING)

    case upgrade: UpgradeSkill ⇒
      val transform = (state: Option[AccountState]) ⇒ {
        val newState = upgradeSkill(state, upgrade.skillType, config.account)
        sendStat(Statistics.buySkill(upgrade.skillType, newState.skills.find(_.skillType == upgrade.skillType).get.level)) // todo: send stat in closure
        newState
      }
      getAndUpdate(transform)

    case buy: BuyItem ⇒
      getAndUpdate(state ⇒ buyItem(state, buy.itemType, config.account))
      sendStat(Statistics.buyItem(buy.itemType))

    case EnterGame() ⇒ sendGameOrder()

  })

  def enterGame: Receive = logged(persistent orElse {
    case msg: AccountResponse ⇒
      val state = msg.state getOrElse config.account.initState
      val rating = msg.rating getOrElse config.account.initRating
      send(matchmaking, GameOrder(client.accountId, client.deviceType, client.userInfo, state, rating, isBot = false))

    case ConnectToGame(gameRef) ⇒
      _game = Some(gameRef)
      send(client.ref, EnteredGame(nodeLocator))

    case JoinGame() ⇒
      send(game, Join(client.accountId, client.ref))
      become(inGame, "inGame")

  })

  def inGame: Receive = logged(persistent orElse {
    case msg: GameMsg ⇒ forward(game, msg)

    case stat: protos.Stat ⇒
      forward(game, stat.action)
      forward(graphite, stat.action)

    case AccountLeaveGame ⇒
      send(client.ref, LeavedGame())
      become(account, "account")

  })

  def persistent: Receive = {
    case AccountStateUpdated(accountId, state) ⇒
      send(client.ref, state)

    case AccountStateAndRatingUpdated(accountId, state, newRating, newPlace, top) ⇒
      send(client.ref, state)
      send(client.ref, Place(newPlace))
      send(client.ref, top.dto)

    case stat: protos.Stat ⇒ sendStat(stat.action)

    case state: TutorState ⇒
      send(storage, Storage.ReplaceTutorState(client.accountId, state))

    case DuplicateAccount ⇒ send(client.ref, PoisonPill)
  }

  def sendStat(statAction: StatAction): Unit =
    send(graphite, statAction)

  def getAndUpdate(transform: Option[AccountState] ⇒ AccountState): Unit =
    send(storage, GetAndUpdateAccountState(client.accountId, transform))

  def sendGameOrder(): Unit = {
    send(storage, GetAccount(client.accountId)) // todo нужно только accountState и rating, не надо запрашивать весь account
    become(enterGame, "enterGame")
  }

  def nodeLocator = NodeLocator(config.host, config.gamePort)
}

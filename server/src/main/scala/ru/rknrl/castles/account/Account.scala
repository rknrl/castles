//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account

import akka.actor.ActorRef
import ru.rknrl.castles.Config
import ru.rknrl.castles.MatchMaking._
import ru.rknrl.castles.account.Account.{DuplicateAccount, LeaveGame}
import ru.rknrl.castles.database.Database._
import ru.rknrl.castles.database.{Database, Statistics}
import ru.rknrl.castles.game.Game.Join
import ru.rknrl.castles.matchmaking.NewMatchmaking.{ConnectToGame, Offline, InGame}
import ru.rknrl.castles.rmi.B2C._
import ru.rknrl.castles.rmi.C2B._
import ru.rknrl.castles.rmi.{B2C, C2B}
import ru.rknrl.core.Stat
import ru.rknrl.core.rmi.CloseConnection
import ru.rknrl.core.social.SocialAuth
import ru.rknrl.dto._
import ru.rknrl.{BugType, EscalateStrategyActor, Logged, SilentLog}

object Account {

  case class LeaveGame(usedItems: Map[ItemType, Int],
                       reward: Int,
                       newRating: Double,
                       top: Iterable[TopUserInfoDTO])

  case object DuplicateAccount

}

class Account(matchmaking: ActorRef,
              database: ActorRef,
              bugs: ActorRef,
              config: Config,
              name: String) extends EscalateStrategyActor {

  val log = new SilentLog

  def logged(r: Receive) = new Logged(r, log, Some(bugs), Some(BugType.ACCOUNT), any ⇒ true)

  // auth

  var client: ActorRef = null
  var accountId: AccountId = null
  var deviceType: DeviceType = null
  var platformType: PlatformType = null
  var userInfo: UserInfoDTO = null
  var state: AccountState = null

  def checkSecret(authenticate: AuthenticateDTO) =
    authenticate.userInfo.accountId.accountType match {
      case AccountType.DEV ⇒
        config.isDev
      case AccountType.VKONTAKTE ⇒
        if (authenticate.secret.accessToken.isDefined)
          true // todo: check access token
        else
          SocialAuth.checkSecretVk(authenticate.secret.body, authenticate.userInfo.accountId.id, config.social.vk.get)

      case AccountType.ODNOKLASSNIKI ⇒
        if (authenticate.secret.accessToken.isDefined)
          true // todo: check access token
        else
          SocialAuth.checkSecretOk(authenticate.secret.body, authenticate.secret.getParams, authenticate.userInfo.accountId.id, config.social.ok.get)

      case AccountType.MOIMIR ⇒
        if (authenticate.secret.accessToken.isDefined)
          true // todo: check access token
        else
          SocialAuth.checkSecretMm(authenticate.secret.body, authenticate.secret.getParams, config.social.mm.get)

      case AccountType.FACEBOOK ⇒
        SocialAuth.checkSecretFb(authenticate.secret.body, authenticate.userInfo.accountId.id, config.social.fb.get)

      case AccountType.DEVICE_ID ⇒
        true

      case _ ⇒ false
    }

  override def receive = auth

  def auth: Receive = logged({
    case Authenticate(authenticate) ⇒
      if (checkSecret(authenticate)) {
        client = sender
        accountId = authenticate.userInfo.accountId
        deviceType = authenticate.deviceType
        platformType = authenticate.platformType
        userInfo = authenticate.userInfo
        database ! Database.GetAccountState(accountId)
        database ! StatAction.AUTHENTICATED
      } else {
        log.info("reject")
        database ! StatAction.NOT_AUTHENTICATED
        sender ! CloseConnection
      }

    case AccountNoExists ⇒
      database ! Insert(accountId, config.account.initAccount.dto, userInfo, TutorStateDTO())
      database ! StatAction.FIRST_AUTHENTICATED

    case AccountStateResponse(id, dto) ⇒
      state = AccountState(dto)
      database ! GetTutorState(accountId)

    case TutorStateResponse(id, tutorState) ⇒
      matchmaking ! InGame(accountId)
      context become enterAccount(tutorState)
  })

  def enterAccount(tutorState: TutorStateDTO): Receive = logged({
    /** Matchmaking ответил на InGame */
    case InGameResponse(game, searchOpponents, top) ⇒
      if (game.isDefined) {
        client ! authenticated(searchOpponents = false, Some(gameAddress), top, tutorState)
        context become inGame(game.get)
      } else if (searchOpponents) {
        client ! authenticated(searchOpponents = true, None, top, tutorState)
        context become enterGame
      } else if (state.gamesCount == 0) {
        placeGameOrder(isTutor = true); // При первом заходе сразу попадаем в бой
        client ! authenticated(searchOpponents = true, None, top, tutorState)
        database ! StatAction.START_TUTOR
        context become enterGame
      } else {
        client ! authenticated(searchOpponents = false, None, top, tutorState)
        context become account
      }
  })

  def authenticated(searchOpponents: Boolean, gameAddress: Option[NodeLocator], top: Iterable[TopUserInfoDTO], tutorState: TutorStateDTO) =
    Authenticated(AuthenticatedDTO(
      accountState = state.dto,
      config = config.account.dto,
      top = TopDTO(top.toSeq),
      products = config.productsDto(platformType, accountId.accountType),
      tutor = tutorState,
      searchOpponents = searchOpponents,
      game = gameAddress
    ))

  def account: Receive = persistent.orElse(logged({
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
      updateState(state.upgradeSkill(dto.`type`, config.account))
      database ! Statistics.buySkill(dto.`type`, state.skills(dto.`type`))

    case BuyItem(dto) ⇒
      updateState(state.buyItem(dto.`type`, config.account))
      database ! Statistics.buyItem(dto.`type`)

    case EnterGame ⇒
      placeGameOrder(isTutor = false)
      context become enterGame
  }))

  def updateState(newState: AccountState) = {
    state = newState
    database ! UpdateAccountState(accountId, state.dto)
  }

  def enterGame: Receive = persistent.orElse(logged({
    /** Matchmaking говорит к какой игре коннектится */
    case ConnectToGame(game) ⇒
      client ! EnteredGame(gameAddress)
      context become inGame(game)
  }))

  def inGame(game: ActorRef): Receive = logged({
    case msg: GameMsg ⇒
      msg match {
        case C2B.JoinGame ⇒ game ! Join(accountId, sender)
        case _ ⇒ game forward msg
      }

    case msg: UpdateStatistics ⇒
      game forward msg.stat.action
      database forward msg.stat.action

    /** Matchmaking говорит, что для этого игрока бой завершен */
    case LeaveGame(usedItems, reward, newRating, top) ⇒
      client ! B2C.LeavedGame

      state = state.addGold(reward)
        .incGamesCount
        .setNewRating(newRating)

      for ((itemType, count) ← usedItems)
        state = state.addItem(itemType, -count)

      database ! UpdateAccountState(accountId, state.dto)
      client ! TopUpdated(TopDTO(top.toSeq))
      context become account

  }).orElse(persistent)

  def persistent: Receive = logged({
    case msg: UpdateStatistics ⇒
      database forward msg.stat.action

    /** from Database, ответ на Update */
    case AccountStateResponse(_, accountStateDto) ⇒
      client ! AccountStateUpdated(accountStateDto)

    /** from Admin or Payments */
    case AdminSetAccountState(_, accountStateDto) ⇒
      state = AccountState(accountStateDto)
      client ! AccountStateUpdated(accountStateDto)

    case C2B.UpdateTutorState(state) ⇒
      database ! Database.UpdateTutorState(accountId, state)

    /** Matchmaking говорит, что кто-то зашел под этим же аккаунтом - закрываем соединение */
    case DuplicateAccount ⇒
      client ! CloseConnection
  })

  def placeGameOrder(isTutor: Boolean) = {
    val realStat = config.account.skillsToStat(state.skills)
    val stat = if (isTutor)
      new Stat(
        attack = realStat.attack * 3,
        defence = realStat.defence * 3,
        speed = realStat.speed
      )
    else
      realStat
    matchmaking ! GameOrder(accountId, deviceType, userInfo, state.slots, stat, state.items, state.rating, state.gamesCount, isBot = false, isTutor)
  }

  def gameAddress = NodeLocator(config.host, config.gamePort)

  override def postStop() = {
    matchmaking ! Offline(accountId, client)
  }
}

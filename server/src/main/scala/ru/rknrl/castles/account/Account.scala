//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account

import akka.actor.ActorRef
import ru.rknrl.castles.MatchMaking._
import ru.rknrl.castles.account.Account.{DuplicateAccount, LeaveGame}
import ru.rknrl.castles.account.state.AccountState
import ru.rknrl.castles.database.Database
import ru.rknrl.castles.database.Database._
import ru.rknrl.castles.game.Game
import ru.rknrl.castles.payments.BugType
import ru.rknrl.castles.rmi.B2C._
import ru.rknrl.castles.rmi.C2B._
import ru.rknrl.castles.rmi.{B2C, C2B}
import ru.rknrl.castles.{AccountId, Config}
import ru.rknrl.core.rmi.CloseConnection
import ru.rknrl.core.social.SocialAuth
import ru.rknrl.dto.AccountDTO._
import ru.rknrl.dto.AuthDTO._
import ru.rknrl.dto.CommonDTO._
import ru.rknrl.{EscalateStrategyActor, Logged, SilentLog}

import scala.collection.JavaConverters._

object Account {

  case class LeaveGame(usedItems: Map[ItemType, Int], reward: Int, newRating: Double, top: Iterable[TopUserInfoDTO])

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
    authenticate.getUserInfo.getAccountId.getType match {
      case AccountType.DEV ⇒
        config.isDev
      case AccountType.VKONTAKTE ⇒
        if (authenticate.getSecret.hasAccessToken)
          true // todo: check access token
        else
          SocialAuth.checkSecretVk(authenticate.getSecret.getBody, authenticate.getUserInfo.getAccountId.getId, config.social.vk.get)

      case AccountType.ODNOKLASSNIKI ⇒
        if (authenticate.getSecret.hasAccessToken)
          true // todo: check access token
        else
          SocialAuth.checkSecretOk(authenticate.getSecret.getBody, authenticate.getSecret.getParams, authenticate.getUserInfo.getAccountId.getId, config.social.ok.get)

      case AccountType.MOIMIR ⇒
        if (authenticate.getSecret.hasAccessToken)
          true // todo: check access token
        else
          SocialAuth.checkSecretMm(authenticate.getSecret.getBody, authenticate.getSecret.getParams, config.social.mm.get)

      case AccountType.FACEBOOK ⇒
        SocialAuth.checkSecretFb(authenticate.getSecret.getBody, authenticate.getUserInfo.getAccountId.getId, config.social.fb.get)

      case AccountType.DEVICE_ID ⇒
        true

      case _ ⇒ false
    }

  override def receive = auth

  def auth: Receive = logged({
    case Authenticate(authenticate) ⇒
      if (checkSecret(authenticate)) {
        client = sender
        accountId = new AccountId(authenticate.getUserInfo.getAccountId)
        deviceType = authenticate.getDeviceType
        platformType = authenticate.getPlatformType
        userInfo = authenticate.getUserInfo
        database ! Database.GetAccountState(accountId.dto)
      } else {
        log.info("reject")
        sender ! CloseConnection
      }

    case AccountNoExists ⇒
      val initTutorState = TutorStateDTO.newBuilder().build()
      database ! Insert(accountId.dto, AccountState.initAccount(config.account).dto, userInfo, initTutorState)

    case AccountStateResponse(id, dto) ⇒
      state = AccountState(dto)
      database ! GetTutorState(accountId.dto)

    case TutorStateResponse(id, tutorState) ⇒
      matchmaking ! InGame(accountId)
      context become enterAccount(tutorState)
  })

  def enterAccount(tutorState: TutorStateDTO): Receive = logged({
    /** Matchmaking ответил на InGame */
    case InGameResponse(game, searchOpponents, top) ⇒
      if (game.isDefined) {
        client ! authenticated(searchOpponents = false, Some(gameAddress), top, tutorState)
        connectToGame(game.get)
      } else if (searchOpponents) {
        client ! authenticated(searchOpponents = true, None, top, tutorState)
        context become enterGame
      } else if (state.gamesCount == 0) {
        placeGameOrder(isTutor = true); // При первом заходе сразу попадаем в бой
        client ! authenticated(searchOpponents = true, None, top, tutorState)
        context become enterGame
      } else {
        client ! authenticated(searchOpponents = false, None, top, tutorState)
        context become account
      }
  })

  def authenticated(searchOpponents: Boolean, gameAddress: Option[NodeLocator], top: Iterable[TopUserInfoDTO], tutorState: TutorStateDTO) = {
    val builder = AuthenticatedDTO.newBuilder()
      .setAccountState(state.dto)
      .setConfig(config.account.dto)
      .setTop(TopDTO.newBuilder().addAllUsers(top.asJava).build)
      .setTutor(tutorState)
      .addAllProducts(config.productsDto(platformType, accountId.accountType).asJava)
      .setSearchOpponents(searchOpponents)

    if (gameAddress.isDefined) builder.setGame(gameAddress.get)

    Authenticated(builder.build)
  }

  def account: Receive = persistent.orElse(logged({
    case BuyBuilding(buy: BuyBuildingDTO) ⇒
      updateState(state.buyBuilding(buy.getId, buy.getBuildingType, config.account))

    case UpgradeBuilding(dto: UpgradeBuildingDTO) ⇒
      updateState(state.upgradeBuilding(dto.getId, config.account))

    case RemoveBuilding(dto: RemoveBuildingDTO) ⇒
      updateState(state.removeBuilding(dto.getId))

    case UpgradeSkill(upgrade: UpgradeSkillDTO) ⇒
      updateState(state.upgradeSkill(upgrade.getType, config.account))

    case BuyItem(buy: BuyItemDTO) ⇒
      updateState(state.buyItem(buy.getType, config.account))

    case EnterGame ⇒
      placeGameOrder(isTutor = false)
      context become enterGame
  }))

  def updateState(newState: AccountState) = {
    state = newState
    database ! UpdateAccountState(accountId.dto, state.dto)
  }

  def enterGame: Receive = persistent.orElse(logged({
    /** Matchmaking говорит к какой игре коннектится */
    case ConnectToGame(game) ⇒
      client ! EnteredGame(gameAddress)
      connectToGame(game)
  }))

  def inGame(game: ActorRef): Receive = persistent.orElse(logged({
    case msg: GameMsg ⇒ game forward msg

    /** Matchmaking говорит, что для этого игрока бой завершен */
    case LeaveGame(usedItems, reward, newRating, top) ⇒
      client ! B2C.LeavedGame

      state = state.addGold(reward)
        .incGamesCount
        .setNewRating(newRating)

      for ((itemType, count) ← usedItems)
        state = state.addItem(itemType, -count)

      database ! UpdateAccountState(accountId.dto, state.dto)
      client ! TopUpdated(TopDTO.newBuilder().addAllUsers(top.asJava).build())
      context become account
  }))

  def persistent: Receive = logged({
    /** from Database, ответ на Update */
    case AccountStateResponse(_, accountStateDto) ⇒
      client ! AccountStateUpdated(accountStateDto)

    /** from Admin or Payments */
    case AdminSetAccountState(_, accountStateDto) ⇒
      state = AccountState(accountStateDto)
      client ! AccountStateUpdated(accountStateDto)

    case C2B.UpdateTutorState(state) ⇒
      database ! Database.UpdateTutorState(accountId.dto, state)

    /** Matchmaking говорит, что кто-то зашел под этим же аккаунтом - закрываем соединение */
    case DuplicateAccount ⇒
      client ! CloseConnection
  })

  def placeGameOrder(isTutor: Boolean) =
    matchmaking ! PlaceGameOrder(new GameOrder(accountId, deviceType, userInfo, state.slots, state.skills.stat(config.account), state.items, state.rating, state.gamesCount, isBot = false, isTutor))

  def connectToGame(game: ActorRef) = {
    game ! Game.Join(accountId, client)
    context become inGame(game)
  }

  def gameAddress = NodeLocator.newBuilder()
    .setHost(config.host)
    .setPort(config.gamePort)
    .build()

  override def postStop() = {
    matchmaking ! Offline(accountId)
  }
}

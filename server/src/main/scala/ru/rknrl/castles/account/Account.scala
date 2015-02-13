//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account

import akka.actor.{ActorLogging, ActorRef}
import akka.pattern.Patterns
import ru.rknrl.EscalateStrategyActor
import ru.rknrl.castles.MatchMaking._
import ru.rknrl.castles.account.Account.{DuplicateAccount, LeaveGame}
import ru.rknrl.castles.account.state.AccountState
import ru.rknrl.castles.database.Database._
import ru.rknrl.castles.game.Game
import ru.rknrl.castles.payments.PaymentsServer.{AddProduct, DatabaseError, ProductAdded}
import ru.rknrl.castles.rmi.B2C.{AccountStateUpdated, Authenticated, EnteredGame}
import ru.rknrl.castles.rmi.C2B._
import ru.rknrl.castles.rmi.{B2C, C2B}
import ru.rknrl.castles.{AccountId, Config}
import ru.rknrl.core.rmi.CloseConnection
import ru.rknrl.core.social.SocialAuth
import ru.rknrl.dto.AccountDTO._
import ru.rknrl.dto.AuthDTO.{AuthenticateDTO, AuthenticatedDTO, TopUserInfoDTO}
import ru.rknrl.dto.CommonDTO._

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration._

object Account {

  case class LeaveGame(usedItems: Map[ItemType, Int], reward: Int, newRating: Double)

  case object DuplicateAccount

}

class Account(matchmaking: ActorRef,
              database: ActorRef,
              config: Config,
              name: String) extends EscalateStrategyActor with ActorLogging {

  // auth

  var client: ActorRef = null
  var accountId: AccountId = null
  var deviceType: DeviceType = null
  var userInfo: UserInfoDTO = null
  var state: AccountState = null
  var top: Iterable[TopUserInfoDTO] = null

  var game: Option[ActorRef] = None

  def checkSecret(authenticate: AuthenticateDTO) =
    authenticate.getUserInfo.getAccountId.getType match {
      case AccountType.DEV ⇒
        true
      case AccountType.VKONTAKTE ⇒
        SocialAuth.checkSecretVk(authenticate.getSecret.getBody, authenticate.getUserInfo.getAccountId.getId, config.social.vk.get)

      case AccountType.ODNOKLASSNIKI ⇒
        SocialAuth.checkSecretOk(authenticate.getSecret.getBody, authenticate.getSecret.getParams, authenticate.getUserInfo.getAccountId.getId, config.social.ok.get)

      case AccountType.MOIMIR ⇒
        SocialAuth.checkSecretMm(authenticate.getSecret.getBody, authenticate.getSecret.getParams, config.social.mm.get)

      case _ ⇒ false
    }

  override def receive = auth

  def auth: Receive = {
    /** from Client */
    case Authenticate(authenticate) ⇒
      if (checkSecret(authenticate)) {
        client = sender
        accountId = new AccountId(authenticate.getUserInfo.getAccountId)
        deviceType = authenticate.getDeviceType
        userInfo = authenticate.getUserInfo
        database ! Get(accountId.dto)
      } else {
        log.info("reject")
        sender ! CloseConnection
      }

    /** from Database */
    case NoExist ⇒
      database ! Insert(accountId.dto, AccountState.initAccount(config.account).dto, userInfo)

    /** from Database */
    case StateResponse(id, dto) ⇒
      state = AccountState.fromDto(dto)
      matchmaking ! InGame(accountId)
      context become enterAccount
  }

  def enterAccount: Receive = {
    /** Matchmaking ответил на InGame */
    case InGameResponse(game, searchOpponents, top) ⇒
      this.top = top
      if (game.isDefined) {
        client ! authenticated(searchOpponents = false, Some(gameAddress), top)
        connectToGame(game.get)
      } else if (searchOpponents) {
        client ! authenticated(searchOpponents = true, None, top)
        context become enterGame
      } else if (state.gamesCount == 0) {
        placeGameOrder(); // При первом заходе сразу попадаем в бой
        client ! authenticated(searchOpponents = true, None, top)
        context become enterGame
      } else {
        client ! authenticated(searchOpponents = false, None, top)
        context become account
      }
  }

  def authenticated(searchOpponents: Boolean, gameAddress: Option[NodeLocator], top: Iterable[TopUserInfoDTO]) = {
    val builder = AuthenticatedDTO.newBuilder()
      .setAccountState(state.dto)
      .setConfig(config.account.dto)
      .addAllTop(top.asJava)
      .addAllProducts(config.productsDto(accountId.accountType).asJava)
      .setSearchOpponents(searchOpponents)

    if (gameAddress.isDefined) builder.setGame(gameAddress.get)

    Authenticated(builder.build)
  }

  def account: Receive = persistent.orElse {
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

    case C2B.EnterGame ⇒
      placeGameOrder()
      context become enterGame
  }

  def updateState(newState: AccountState) = {
    state = newState
    database ! Update(accountId.dto, state.dto)
  }

  def enterGame: Receive = persistent.orElse {
    /** Matchmaking говорит к какой игре коннектится */
    case ConnectToGame(game) ⇒
      client ! EnteredGame(gameAddress)
      connectToGame(game)
      context become inGame
  }

  def inGame: Receive = persistent.orElse {
    case msg: GameMsg ⇒ game.get forward msg

    /** Matchmaking говорит, что для этого игрока бой завершен */
    case LeaveGame(usedItems, reward, newRating) ⇒
      assert(game.isDefined)

      client ! B2C.LeavedGame

      state = state.addGold(reward)
        .incGamesCount
        .setNewRating(newRating)

      for ((itemType, count) ← usedItems)
        state = state.addItem(itemType, -count)

      game = None

      database ! Update(accountId.dto, state.dto)
      context become account
  }

  def persistent: Receive = {
    /** from Database, ответ на Update */
    case StateResponse(_, accountStateDto) ⇒
      client ! AccountStateUpdated(accountStateDto)

    /** from Admin */
    case AdminSetAccountState(_, accountStateDto) ⇒
      this.state = AccountState.fromDto(accountStateDto)
      client ! AccountStateUpdated(accountStateDto)

    case AddProduct(accountId, orderId, product, count) ⇒
      log.info("AddProduct")
      product.id match {
        case 1 ⇒ state = state.addGold(count)
        case _ ⇒ throw new IllegalArgumentException("unknown product id " + product.id)
      }

      val future = Patterns.ask(database, Update(accountId.dto, state.dto), 5 seconds)
      val result = Await.result(future, 5 seconds)

      result match {
        case StateResponse(_, accountStateDto) ⇒
          if (accountStateDto.getGold == state.gold) {
            sender ! ProductAdded(orderId)
            client ! AccountStateUpdated(accountStateDto)
          } else {
            log.info("invalid gold=" + accountStateDto.getGold + ", but expected " + state.gold)
            sender ! DatabaseError
          }
        case invalid ⇒
          log.info("invalid result=" + invalid)
          sender ! DatabaseError
      }

    /** Matchmaking говорит, что кто-то зашел под этим же аккаунтом - закрываем соединение */
    case DuplicateAccount ⇒ client ! CloseConnection
  }

  def placeGameOrder() =
    matchmaking ! PlaceGameOrder(new GameOrder(accountId, deviceType, userInfo, state.slots, state.skills, state.items, state.rating, state.gamesCount, isBot = false))

  def connectToGame(game: ActorRef) = {
    assert(this.game.isEmpty)
    game ! Game.Join(accountId, client)
    this.game = Some(game)
    context become inGame
  }

  def gameAddress = NodeLocator.newBuilder()
    .setHost(config.host)
    .setPort(config.gamePort)
    .build()

  override def postStop() = {
    log.info("account stop")
    matchmaking ! Offline(accountId)
  }
}

package ru.rknrl.castles.account

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.Patterns
import ru.rknrl.EscalateStrategyActor
import ru.rknrl.castles.MatchMaking._
import ru.rknrl.castles._
import ru.rknrl.castles.database.AccountStateDb.Put
import ru.rknrl.castles.game.Game.{Join, Offline}
import ru.rknrl.castles.payments.PaymentsServer.{AddProduct, ProductAdded}
import ru.rknrl.castles.rmi._
import ru.rknrl.core.rmi.{ReceiverRegistered, RegisterReceiver, UnregisterReceiver}
import ru.rknrl.dto.AccountDTO._
import ru.rknrl.dto.AuthDTO.{AuthenticationSuccessDTO, TopUserInfoDTO}
import ru.rknrl.dto.CommonDTO._

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration._

case object GetAccountState

case class LeaveGame(usedItems: Map[ItemType, Int], reward: Int)

class Account(accountId: AccountId,
              accountState: AccountState,
              deviceType: DeviceType,
              userInfo: UserInfoDTO,
              tcpSender: ActorRef, tcpReceiver: ActorRef,
              matchmaking: ActorRef,
              accountStateDb: ActorRef,
              auth: ActorRef,
              config: Config,
              name: String) extends EscalateStrategyActor {

  private var accountRmiRegistered: Boolean = false

  private val accountRmi = context.actorOf(Props(classOf[AccountRMI], tcpSender, self), "account-rmi" + name)
  tcpReceiver ! RegisterReceiver(accountRmi)

  private var state = accountState

  private var reenterGame: Boolean = true

  private var enterGameRmiRegistered: Boolean = false

  private var enterGameRmi: Option[ActorRef] = None

  private var gameRmiRegistered: Boolean = false

  private var gameRmi: Option[ActorRef] = None

  private var game: Option[ActorRef] = None

  def receive = {
    case EnterGameMsg() ⇒
      matchmaking ! PlaceGameOrder(new GameOrder(accountId, deviceType, userInfo, state.startLocation, state.skills, state.items, isBot = false))

    /**
     * from accountStateDb
     */
    case Put(accountId, accountStateDto) ⇒
      accountRmi ! AccountStateUpdatedMsg(accountStateDto)

    case SwapSlotsMsg(swap: SwapSlotsDTO) ⇒
      state = state.swapSlots(swap.getId1, swap.getId2)
      accountStateDb ! Put(accountId, state.dto)

    case BuyBuildingMsg(buy: BuyBuildingDTO) ⇒
      state = state.buyBuilding(buy.getId, buy.getBuildingType)
      accountStateDb ! Put(accountId, state.dto)

    case UpgradeBuildingMsg(dto: UpgradeBuildingDTO) ⇒
      state = state.upgradeBuilding(dto.getId)
      accountStateDb ! Put(accountId, state.dto)

    case RemoveBuildingMsg(dto: RemoveBuildingDTO) ⇒
      state = state.removeBuilding(dto.getId)
      accountStateDb ! Put(accountId, state.dto)

    case UpgradeSkillMsg(upgrade: UpgradeSkillDTO) ⇒
      state = state.upgradeSkill(upgrade.getType, config.account)
      accountStateDb ! Put(accountId, state.dto)

    case BuyItemMsg(buy: BuyItemDTO) ⇒
      state = state.buyItem(buy.getType)
      accountStateDb ! Put(accountId, state.dto)

    case AddProduct(orderId, product, count) ⇒
      product.id match {
        case 1 ⇒ state = state.addGold(count)
        case _ ⇒ throw new IllegalArgumentException("unknown product id " + product.id)
      }

      val future = Patterns.ask(accountStateDb, Put(accountId, state.dto), 5 seconds)
      val result = Await.result(future, 5 seconds)

      result match {
        case Put(accountId, accountStateDto) ⇒
          if (accountStateDto.getGold == state.gold) {
            sender ! ProductAdded(orderId)
            accountRmi ! AccountStateUpdatedMsg(accountStateDto)
          } else {
            // send error
          }
        case _ ⇒ // send error
      }

    /**
     * Auth спрашивает accountState
     * Спрашиваем матчмейкинг находится ли этот игрок в бою
     */
    case GetAccountState ⇒
      matchmaking ! InGame(accountId)

    /**
     * Matchmaking ответил на InGame
     * Отправляем Auth accountState
     */
    case InGameResponse(gameRef, enterGame) ⇒
      if (gameRef.isDefined) {
        reenterGame = true
        connectToGame(gameRef.get)
      } else {
        reenterGame = false

        auth ! AuthenticationSuccessDTO.newBuilder()
          .setAccountState(state.dto)
          .setConfig(config.account.dto)
          .addAllTop(topMock.asJava)
          .addAllProducts(config.productsDto(accountId.accountType).asJava)
          .setEnterGame(enterGame)
          .build
      }

    /**
     * Matchmaking говорит к какой игре коннектится
     */
    case ConnectToGame(gameRef) ⇒
      connectToGame(gameRef)

    case ReceiverRegistered(ref) ⇒
      if (ref == accountRmi)
        if (accountRmiRegistered)
          throw new IllegalStateException("accountRmi already registered")
        else
          accountRmiRegistered = true
      else if (ref == enterGameRmi.get)
        if (enterGameRmiRegistered)
          throw new IllegalStateException("enterGameRmi already registered")
        else
          enterGameRmiRegistered = true
      else if (ref == gameRmi.get)
        if (gameRmiRegistered)
          throw new IllegalStateException("gameRmiRegistered already registered")
        else
          gameRmiRegistered = true
      else
        throw new IllegalArgumentException("unknown receiver " + ref)

      sendJoin()

    /**
     * Game говорит, что для этого игрока бой завершен
     */
    case LeaveGame(usedItems, reward) ⇒
      assert(game.isDefined)
      assert(enterGameRmi.isDefined)
      assert(gameRmi.isDefined)

      enterGameRmi.get ! LeaveGameMsg()

      state = state.addGold(reward)
      for ((itemType, count) ← usedItems)
        state = state.addItem(itemType, -count)

      tcpReceiver ! UnregisterReceiver(enterGameRmi.get)
      context stop enterGameRmi.get

      tcpReceiver ! UnregisterReceiver(gameRmi.get)
      context stop gameRmi.get

      game = None

      enterGameRmi = None
      enterGameRmiRegistered = false

      gameRmi = None
      gameRmiRegistered = false

      accountStateDb ! Put(accountId, state.dto)
  }

  private def connectToGame(game: ActorRef) = {
    assert(this.game.isEmpty)
    assert(this.enterGameRmi.isEmpty)
    assert(this.gameRmi.isEmpty)

    val enterGameRmi = context.actorOf(Props(classOf[EnterGameRMI], tcpSender, game), "enter-game-rmi" + name)
    tcpReceiver ! RegisterReceiver(enterGameRmi)

    val gameRmi = context.actorOf(Props(classOf[GameRMI], tcpSender, game), "game-rmi" + name)
    tcpReceiver ! RegisterReceiver(gameRmi)

    game ! Join(accountId, enterGameRmi, gameRmi)

    this.game = Some(game)
    this.enterGameRmi = Some(enterGameRmi)
    this.gameRmi = Some(gameRmi)
  }

  private def sendJoin(): Unit =
    if (accountRmiRegistered && enterGameRmiRegistered && gameRmiRegistered) {
      val gameAddress = NodeLocator.newBuilder()
        .setHost(config.host)
        .setPort(config.gamePort)
        .build()

      if (reenterGame) {
        auth ! AuthenticationSuccessDTO.newBuilder()
          .setAccountState(state.dto)
          .setConfig(config.account.dto)
          .addAllTop(topMock.asJava)
          .addAllProducts(config.productsDto(accountId.accountType).asJava)
          .setEnterGame(false)
          .setGame(gameAddress)
          .build

        reenterGame = false
      } else
        accountRmi ! EnteredGameMsg(gameAddress)
    }

  private def topMock =
    for (i ← 1 to 5)
    yield TopUserInfoDTO.newBuilder()
      .setPlace(i)
      .setInfo(
        UserInfoDTO.newBuilder()
          .setAccountId(
            AccountIdDTO.newBuilder()
              .setId("1")
              .setType(AccountType.DEV)
              .build()
          )
          .setFirstName("name" + i)
          .setPhoto256(i.toString)
          .build()
      ).build()


  override def preStart(): Unit = println("AccountService start " + name)

  override def postStop(): Unit = {
    if (game.isDefined) game.get ! Offline(accountId)
    println("AccountService stop " + name)
  }
}
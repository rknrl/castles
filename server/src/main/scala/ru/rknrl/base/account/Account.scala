package ru.rknrl.base.account

import akka.actor.{ActorRef, Props}
import akka.pattern.Patterns
import ru.rknrl.EscalateStrategyActor
import ru.rknrl.base.game.Game.{Join, Offline}
import ru.rknrl.castles.MatchMaking._
import ru.rknrl.castles.account.AccountState
import ru.rknrl.castles.database.AccountStateDb.Put
import ru.rknrl.base.payments.PaymentsServer.{AddProduct, ProductAdded}
import ru.rknrl.castles.rmi._
import ru.rknrl.castles.{AccountId, Config}
import ru.rknrl.core.rmi.{ReceiverRegistered, RegisterReceiver, UnregisterReceiver}
import ru.rknrl.dto.AuthDTO.AuthenticationSuccessDTO
import ru.rknrl.dto.CommonDTO.{DeviceType, ItemType, NodeLocator, UserInfoDTO}

import scala.concurrent.Await
import scala.concurrent.duration._

case object GetAccountState

case class LeaveGame(usedItems: Map[ItemType, Int], reward: Int)

abstract class Account(accountId: AccountId,
                       deviceType: DeviceType,
                       userInfo: UserInfoDTO,
                       tcpSender: ActorRef, tcpReceiver: ActorRef,
                       matchmaking: ActorRef,
                       accountStateDb: ActorRef,
                       auth: ActorRef,
                       config: Config,
                       name: String) extends EscalateStrategyActor {

  private val accountRmi = context.actorOf(Props(classOf[AccountRMI], tcpSender, self), "account-rmi" + name)
  tcpReceiver ! RegisterReceiver(accountRmi)

  protected var state: AccountState

  private var accountRmiRegistered: Boolean = false

  private var reenterGame: Boolean = true

  private var enterGameRmiRegistered: Boolean = false

  private var enterGameRmi: Option[ActorRef] = None

  private var gameRmiRegistered: Boolean = false

  private var gameRmi: Option[ActorRef] = None

  private var game: Option[ActorRef] = None

  protected def authenticationSuccessDto(enterGame: Boolean, gameAddress: Option[NodeLocator]): AuthenticationSuccessDTO

  override def receive = {
    case EnterGameMsg() ⇒
      matchmaking ! PlaceGameOrder(new GameOrder(accountId, deviceType, userInfo, state.startLocation, state.skills, state.items, isBot = false))

    /**
     * from accountStateDb
     */
    case Put(accountId, accountStateDto) ⇒
      accountRmi ! AccountStateUpdatedMsg(accountStateDto)

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

        auth ! authenticationSuccessDto(enterGame, None)
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
        auth ! authenticationSuccessDto(enterGame = false, Some(gameAddress))

        reenterGame = false
      } else
        accountRmi ! EnteredGameMsg(gameAddress)
    }

  override def preStart(): Unit = println("AccountService start " + name)

  override def postStop(): Unit = {
    if (game.isDefined) game.get ! Offline(accountId)
    println("AccountService stop " + name)
  }
}

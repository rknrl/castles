package ru.rknrl.base.account

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.pattern.Patterns
import ru.rknrl.EscalateStrategyActor
import ru.rknrl.base.AccountId
import ru.rknrl.base.MatchMaking._
import ru.rknrl.base.account.Account.{DuplicateAccount, GetAuthenticationSuccess, LeaveGame}
import ru.rknrl.base.database.AccountStateDb.{StateResponse, Update}
import ru.rknrl.base.game.Game.{Join, Offline}
import ru.rknrl.base.payments.PaymentsServer.{AddProduct, DatabaseError, ProductAdded}
import ru.rknrl.castles.Config
import ru.rknrl.castles.account.AccountState
import ru.rknrl.castles.rmi._
import ru.rknrl.core.rmi.{CloseConnection, ReceiverRegistered, RegisterReceiver, UnregisterReceiver}
import ru.rknrl.dto.AuthDTO.{AuthenticationSuccessDTO, TopUserInfoDTO}
import ru.rknrl.dto.CommonDTO.{DeviceType, ItemType, NodeLocator, UserInfoDTO}

import scala.concurrent.Await
import scala.concurrent.duration._

object Account {

  case object GetAuthenticationSuccess

  case class LeaveGame(usedItems: Map[ItemType, Int], reward: Int, newRating: Double)

  case object DuplicateAccount

}

abstract class Account(accountId: AccountId,
                       deviceType: DeviceType,
                       userInfo: UserInfoDTO,
                       tcpSender: ActorRef, tcpReceiver: ActorRef,
                       matchmaking: ActorRef,
                       accountStateDb: ActorRef,
                       auth: ActorRef,
                       config: Config,
                       name: String) extends EscalateStrategyActor with ActorLogging {

  private val accountRmi = context.actorOf(Props(classOf[AccountRMI], tcpSender, self), "account-rmi" + name)
  tcpReceiver ! RegisterReceiver(accountRmi)

  protected var state: AccountState

  private var accountRmiRegistered: Boolean = false

  private var reenterGame: Boolean = true

  private var top: Option[Iterable[TopUserInfoDTO]] = None

  private var enterGameRmiRegistered: Boolean = false

  private var enterGameRmi: Option[ActorRef] = None

  private var gameRmiRegistered: Boolean = false

  private var gameRmi: Option[ActorRef] = None

  private var game: Option[ActorRef] = None

  protected def authenticationSuccessDto(enterGame: Boolean, gameAddress: Option[NodeLocator], top: Iterable[TopUserInfoDTO]): AuthenticationSuccessDTO

  private def placeGameOrder() =
    matchmaking ! PlaceGameOrder(new GameOrder(accountId, deviceType, userInfo, state.startLocation, state.skills, state.items, state.rating, state.gamesCount, isBot = false))

  override def receive = {
    case EnterGameMsg() ⇒ placeGameOrder()

    /** from AccountStateDb, ответ на Update */
    case StateResponse(_, accountStateDto) ⇒
      accountRmi ! AccountStateUpdatedMsg(accountStateDto)

    case AddProduct(accountId, orderId, product, count) ⇒
      log.info("AddProduct")
      product.id match {
        case 1 ⇒ state = state.addGold(count)
        case _ ⇒ throw new IllegalArgumentException("unknown product id " + product.id)
      }

      val future = Patterns.ask(accountStateDb, Update(accountId.dto, state.dto), 5 seconds)
      val result = Await.result(future, 5 seconds)

      result match {
        case StateResponse(_, accountStateDto) ⇒
          if (accountStateDto.getGold == state.gold) {
            sender ! ProductAdded(orderId)
            accountRmi ! AccountStateUpdatedMsg(accountStateDto)
          } else {
            log.info("invalid gold=" + accountStateDto.getGold + ", but expected " + state.gold)
            sender ! DatabaseError
          }
        case invalid ⇒
          log.info("invalid result=" + invalid)
          sender ! DatabaseError
      }

    /** Auth спрашивает accountState
      * Спрашиваем матчмейкинг находится ли этот игрок в бою
      */
    case GetAuthenticationSuccess ⇒
      matchmaking ! InGame(accountId, deviceType)

    /** Matchmaking ответил на InGame
      * Отправляем Auth accountState
      */
    case InGameResponse(gameRef, enterGame, top) ⇒
      this.top = Some(top)
      if (gameRef.isDefined) {
        reenterGame = true
        connectToGame(gameRef.get)
      } else if (!enterGame && state.gamesCount == 0) {
        placeGameOrder(); // При первом заходе сразу попадаем в бой
        reenterGame = false
        auth ! authenticationSuccessDto(enterGame = true, None, top)
      } else {
        reenterGame = false
        auth ! authenticationSuccessDto(enterGame, None, top)
      }

    /** Matchmaking говорит к какой игре коннектится */
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

    /** Matchmaking говорит, что для этого игрока бой завершен */
    case LeaveGame(usedItems, reward, newRating) ⇒
      assert(game.isDefined)
      assert(enterGameRmi.isDefined)
      assert(gameRmi.isDefined)

      enterGameRmi.get ! LeaveGameMsg()

      state = state.addGold(reward)
        .incGamesCount
        .setNewRating(newRating)

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

      accountStateDb ! Update(accountId.dto, state.dto)

    /** Matchmaking говорит, что кто-то зашел под этим же аккаунтом - закрываем соединение */
    case DuplicateAccount ⇒ tcpReceiver ! CloseConnection
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
        auth ! authenticationSuccessDto(enterGame = false, Some(gameAddress), top.get)

        reenterGame = false
      } else
        accountRmi ! EnteredGameMsg(gameAddress)
    }

  override def postStop() = if (game.isDefined) game.get ! Offline(accountId)
}

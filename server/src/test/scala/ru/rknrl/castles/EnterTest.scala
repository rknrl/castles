package ru.rknrl.castles

import java.net.InetSocketAddress

import akka.actor.{ActorSystem, Props}
import akka.io.Tcp.Connect
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import ru.rknrl.castles.database.InMemoryDb
import ru.rknrl.castles.rmi._
import ru.rknrl.core.rmi.testkit.{ClientConnected, ServerBounded, TcpClientMock, TcpMock}
import ru.rknrl.core.rmi.{ReceiverRegistered, RegisterReceiver, TcpReceiver}
import ru.rknrl.dto.AccountDTO._
import ru.rknrl.dto.AuthDTO._
import ru.rknrl.dto.CommonDTO._

import scala.concurrent.duration._

class EnterTest
  extends TestKit(ActorSystem("test-actor-system"))
  with WordSpecLike
  with DefaultTimeout
  with ImplicitSender
  with Matchers
  with BeforeAndAfterAll {

  val configMock = ConfigMock.config

  val matchmaking = system.actorOf(Props(classOf[MatchMaking], 10 millis, configMock.game), "matchmaking")

  "TcpServer" should {
    "accept client connection" +
      "response with AuthReadyMsg" in {

      val accountStateDb = system.actorOf(Props(classOf[InMemoryDb]), "account-state-db")

      // create tcp connection

      val tcpMock = system.actorOf(Props(classOf[TcpMock], testActor), "tcp-mock")

      // create tcp server

      val tcpServer = system.actorOf(Props(classOf[TcpServer], tcpMock, configMock, matchmaking, accountStateDb), "tcpServer")

      expectMsgPF(100 millis) {
        case ServerBounded() ⇒ true
      }

      // create tcp client

      val clientReceiver = system.actorOf(Props(classOf[TcpReceiver], "client-tcp-receiver"), "client-tcp-receiver")

      val tcpClientMock = system.actorOf(Props(classOf[TcpClientMock], clientReceiver, tcpMock), "tcp-client-mock")

      // create client ath rmi

      val authRmiClientMock = system.actorOf(Props(classOf[AuthRMIClientMock], tcpClientMock, testActor), "auth-rmi-client-mock")

      clientReceiver ! RegisterReceiver(authRmiClientMock)

      expectMsgPF(100 millis) {
        case ReceiverRegistered(ref) ⇒ true
      }

      // connect to server

      tcpClientMock ! Connect(new InetSocketAddress("localhost", 12345))

      expectMsgPF(100 millis) {
        case ClientConnected() ⇒ true
      }

      // get auth ready from server

      expectMsgPF(100 millis) {
        case AuthReadyMsg() ⇒ true
      }

      // auth by dev account

      val accountId = AccountIdDTO.newBuilder()
        .setId("1")
        .setType(AccountType.DEV)
        .build()

      val secret = AuthenticationSecretDTO.newBuilder()
        .setBody("secret")
        .build()

      val authenticate = AuthenticateDTO.newBuilder()
        .setUserInfo(
          UserInfoDTO.newBuilder()
            .setAccountId(accountId)
            .build()
        ).setSecret(secret)
        .setDeviceType(DeviceType.CANVAS)
        .build()

      authRmiClientMock ! AuthenticateMsg(authenticate)

      expectMsgPF(1000 millis) {
        case AuthenticationSuccessMsg(state) ⇒ true
      }

      // create client account rmi

      val accountRmiClientMock = system.actorOf(Props(classOf[AccountRMIClientMock], tcpClientMock, testActor), "account-rmi-client-mock")

      clientReceiver ! RegisterReceiver(accountRmiClientMock)

      expectMsgPF(100 millis) {
        case ReceiverRegistered(ref) ⇒ true
      }

      // buy item

      accountRmiClientMock ! BuyItemMsg(BuyItemDTO.newBuilder().setType(ItemType.FIREBALL).build())

      expectMsgPF(1000 millis) {
        case AccountStateUpdatedMsg(dto) ⇒ true
      }

      // upgrade skill

      accountRmiClientMock ! UpgradeSkillMsg(UpgradeSkillDTO.newBuilder().setType(SkillType.ATTACK).build())

      expectMsgPF(1000 millis) {
        case AccountStateUpdatedMsg(dto) ⇒ true
      }

      // upgrade building

      accountRmiClientMock ! UpgradeBuildingMsg(UpgradeBuildingDTO.newBuilder().setId(SlotId.SLOT_1).build())

      expectMsgPF(1000 millis) {
        case AccountStateUpdatedMsg(dto) ⇒ true
      }

      // build building

      accountRmiClientMock ! BuyBuildingMsg(BuyBuildingDTO.newBuilder().setId(SlotId.SLOT_2).setBuildingType(BuildingType.TOWER).build())

      expectMsgPF(1000 millis) {
        case AccountStateUpdatedMsg(dto) ⇒ true
      }

      // swap slots

      accountRmiClientMock ! SwapSlotsMsg(SwapSlotsDTO.newBuilder().setId1(SlotId.SLOT_1).setId2(SlotId.SLOT_2).build())

      expectMsgPF(1000 millis) {
        case AccountStateUpdatedMsg(dto) ⇒ true
      }

      // remove building

      accountRmiClientMock ! RemoveBuildingMsg(RemoveBuildingDTO.newBuilder().setId(SlotId.SLOT_1).build())

      expectMsgPF(1000 millis) {
        case AccountStateUpdatedMsg(dto) ⇒ true
      }

      // enter game

      accountRmiClientMock ! EnterGameMsg()

      expectMsgPF(1000 millis) {
        case EnteredGameMsg(dto) ⇒ true
      }

      // create client enter game rmi

      val enterGameRmiClientMock = system.actorOf(Props(classOf[EnterGameRMIClientMock], tcpClientMock, testActor), "enter-game-rmi-client-mock")

      clientReceiver ! RegisterReceiver(enterGameRmiClientMock)

      expectMsgPF(100 millis) {
        case ReceiverRegistered(ref) ⇒ true
      }

      // join game

      enterGameRmiClientMock ! JoinMsg()

      expectMsgPF(1000 millis) {
        case JoinGameMsg(dto) ⇒ true
      }

      // create client game rmi

      val gameRmiClientMock = system.actorOf(Props(classOf[GameRMIClientMock], tcpClientMock, testActor), "game-rmi-client-mock")

      clientReceiver ! RegisterReceiver(gameRmiClientMock)

      expectMsgPF(100 millis) {
        case ReceiverRegistered(ref) ⇒ true
      }

      // todo test game

      // leave game

      gameRmiClientMock ! SurrenderMsg()

      ignoreMsg {
        case msg: GameOverMsg ⇒ false
        case _ ⇒ true
      }

      expectMsgPF(1000 millis) {
        case GameOverMsg(dto) ⇒ true
      }

      // leave game

      gameRmiClientMock ! LeaveMsg()

      ignoreMsg {
        case msg: LeaveGameMsg ⇒ false
        case _ ⇒ true
      }

      expectMsgPF(1000 millis) {
        case LeaveGameMsg() ⇒ true
      }

    }
  }

  override protected def afterAll(): Unit = shutdown()
}
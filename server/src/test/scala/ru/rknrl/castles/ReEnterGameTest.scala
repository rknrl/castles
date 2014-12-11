package ru.rknrl.castles

import _root_.ru.rknrl.castles.config.ConfigTest
import _root_.ru.rknrl.castles.rmi._
import _root_.ru.rknrl.core.rmi.{RegisterReceiver, TcpReceiver}
import _root_.ru.rknrl.dto.AuthDTO._
import akka.actor.{ActorSystem, Props}
import akka.io.Tcp.PeerClosed
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import ru.rknrl.core.rmi.testkit._

import scala.concurrent.duration._

class ReEnterGameTest
  extends TestKit(ActorSystem("test-actor-system"))
  with WordSpecLike
  with DefaultTimeout
  with ImplicitSender
  with Matchers
  with BeforeAndAfterAll {

  val configMock = ConfigTest.configMock

  val matchmaking = system.actorOf(Props(classOf[MatchMaking], 10 millis, configMock.game), "matchmaking")

  "Клиент, который оборвал соединение во время боя" should {
    "при следующем заходе попасть в бой" in {

      // create tcp connection

      val tcpMock = system.actorOf(Props(classOf[TcpMock], testActor), "tcp-mock")

      val tcpServer = system.actorOf(Props(classOf[TcpServer], tcpMock, configMock, matchmaking), "tcpServer")

      expectMsgPF(100 millis) {
        case ServerBounded() ⇒ true
      }

      val clientReceiver = system.actorOf(Props(classOf[TcpReceiver], "client-tcp-receiver"), "client-tcp-receiver")

      val tcpClientMock = system.actorOf(Props(classOf[TcpClientMock], clientReceiver, tcpMock), "tcp-client-mock")

      expectMsgPF(100 millis) {
        case ClientConnected() ⇒ true
      }

      // create client ath rmi

      val authRmiClientMock = system.actorOf(Props(classOf[AuthRMIClientMock], tcpClientMock, testActor), "auth-rmi-client-mock")

      clientReceiver ! RegisterReceiver(authRmiClientMock, AuthRMIClientMock.allCommands)

      expectMsgPF(100 millis) {
        case AuthRMIClientMockReady() ⇒ true
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
        .setAccountId(accountId)
        .setSecret(secret)
        .setDeviceType(DeviceType.CANVAS)
        .build()

      authRmiClientMock ! AuthenticateMsg(authenticate)

      expectMsgPF(1000 millis) {
        case AuthenticationResultMsg(state) ⇒ true
      }

      // create client account rmi

      val accountRmiClientMock = system.actorOf(Props(classOf[AccountRMIClientMock], tcpClientMock, testActor), "account-rmi-client-mock")

      clientReceiver ! RegisterReceiver(accountRmiClientMock, AccountRMIClientMock.allCommands)

      expectMsgPF(100 millis) {
        case AccountRMIClientMockReady() ⇒ true
      }

      // enter game

      accountRmiClientMock ! EnterGameMsg()

      expectMsgPF(1000 millis) {
        case EnteredGameMsg(dto) ⇒ true
      }

      // create client enter game rmi

      val enterGameRmiClientMock = system.actorOf(Props(classOf[EnterGameRMIClientMock], tcpClientMock, testActor), "enter-game-rmi-client-mock")

      clientReceiver ! RegisterReceiver(enterGameRmiClientMock, EnterGameRMIClientMock.allCommands)

      expectMsgPF(100 millis) {
        case EnterGameRMIClientMockReady() ⇒ true
      }

      // join game

      enterGameRmiClientMock ! JoinMsg()

      expectMsgPF(1000 millis) {
        case JoinGameMsg(dto) ⇒ true
      }

      // create client game rmi

      val gameRmiClientMock = system.actorOf(Props(classOf[GameRMIClientMock], tcpClientMock, testActor), "game-rmi-client-mock")

      clientReceiver ! RegisterReceiver(gameRmiClientMock, GameRMIClientMock.allCommands)

      expectMsgPF(100 millis) {
        case GameRMIClientMockReady() ⇒ true
      }

      // close client connection

      tcpClientMock ! PeerClosed

      system stop tcpClientMock
      system stop clientReceiver
      system stop authRmiClientMock
      system stop accountRmiClientMock
      system stop enterGameRmiClientMock
      system stop gameRmiClientMock

      Thread.sleep(100)

      //--------------------------------------------------
      //
      // Second connection
      //
      //--------------------------------------------------

      // create client tcp connection

      val clientReceiver2 = system.actorOf(Props(classOf[TcpReceiver], "client-tcp-receiver"), "client-tcp-receiver")

      val tcpClientMock2 = system.actorOf(Props(classOf[TcpClientMock], clientReceiver2, tcpMock), "tcp-client-mock")

      expectMsgPF(100 millis) {
        case ClientConnected() ⇒ true
      }

      // create client ath rmi

      val authRmiClientMock2 = system.actorOf(Props(classOf[AuthRMIClientMock], tcpClientMock2, testActor), "auth-rmi-client-mock")

      clientReceiver2 ! RegisterReceiver(authRmiClientMock2, AuthRMIClientMock.allCommands)

      expectMsgPF(100 millis) {
        case AuthRMIClientMockReady() ⇒ true
      }

      // get auth ready from server

      expectMsgPF(100 millis) {
        case AuthReadyMsg() ⇒ true
      }

      // auth by dev account

      authRmiClientMock2 ! AuthenticateMsg(authenticate)

      expectMsgPF(1000 millis) {
        case AuthenticationResultMsg(state) ⇒
          state.hasGame should be(true)
          true
      }

      // create client account rmi

      val accountRmiClientMock2 = system.actorOf(Props(classOf[AccountRMIClientMock], tcpClientMock2, testActor), "account-rmi-client-mock")

      clientReceiver2 ! RegisterReceiver(accountRmiClientMock2, AccountRMIClientMock.allCommands)

      expectMsgPF(100 millis) {
        case AccountRMIClientMockReady() ⇒ true
      }

      // create client enter game rmi

      val enterGameRmiClientMock2 = system.actorOf(Props(classOf[EnterGameRMIClientMock], tcpClientMock2, testActor), "enter-game-rmi-client-mock")

      clientReceiver2 ! RegisterReceiver(enterGameRmiClientMock2, EnterGameRMIClientMock.allCommands)

      expectMsgPF(100 millis) {
        case EnterGameRMIClientMockReady() ⇒ true
      }

      // join game

      enterGameRmiClientMock2 ! JoinMsg()

      expectMsgPF(1000 millis) {
        case JoinGameMsg(dto) ⇒ true
      }

      // create client game rmi

      val gameRmiClientMock2 = system.actorOf(Props(classOf[GameRMIClientMock], tcpClientMock2, testActor), "game-rmi-client-mock")

      clientReceiver2 ! RegisterReceiver(gameRmiClientMock2, GameRMIClientMock.allCommands)

      expectMsgPF(100 millis) {
        case GameRMIClientMockReady() ⇒ true
      }


    }
  }

  override protected def afterAll(): Unit = shutdown()
}
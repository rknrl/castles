package ru.rknrl.castles

import java.net.InetSocketAddress

import _root_.ru.rknrl.castles.rmi._
import _root_.ru.rknrl.dto.AuthDTO._
import akka.actor.{ActorSystem, Props}
import akka.io.Tcp.{Connect, PeerClosed}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import ru.rknrl.base.{MatchMaking, TcpServer}
import ru.rknrl.castles.database.InMemoryDb
import ru.rknrl.castles.mock.ConfigMock
import ru.rknrl.core.rmi.{ReceiverRegistered, RegisterReceiver, TcpReceiver}
import ru.rknrl.core.rmi.testkit._
import _root_.ru.rknrl.dto.AccountDTO._
import _root_.ru.rknrl.dto.AuthDTO._
import _root_.ru.rknrl.dto.CommonDTO._

import scala.concurrent.duration._

class ReEnterGameTest
  extends TestKit(ActorSystem("test-actor-system"))
  with WordSpecLike
  with DefaultTimeout
  with ImplicitSender
  with Matchers
  with BeforeAndAfterAll {

  val configMock = ConfigMock.config

  val matchmaking = system.actorOf(Props(classOf[MatchMaking], 10 millis, configMock.game), "matchmaking")

  "Клиент, который оборвал соединение во время боя" should {
    "при следующем заходе попасть в бой" in {

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
        )
        .setSecret(secret)
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

      // create client tcp

      val clientReceiver2 = system.actorOf(Props(classOf[TcpReceiver], "client-tcp-receiver"), "client-tcp-receiver")

      val tcpClientMock2 = system.actorOf(Props(classOf[TcpClientMock], clientReceiver2, tcpMock), "tcp-client-mock")

      // create client ath rmi

      val authRmiClientMock2 = system.actorOf(Props(classOf[AuthRMIClientMock], tcpClientMock2, testActor), "auth-rmi-client-mock")

      clientReceiver2 ! RegisterReceiver(authRmiClientMock2)

      expectMsgPF(100 millis) {
        case ReceiverRegistered(ref) ⇒ true
      }

      // connect to server

      tcpClientMock2 ! Connect(new InetSocketAddress("localhost", 12345))

      expectMsgPF(100 millis) {
        case ClientConnected() ⇒ true
      }

      // get auth ready from server

      expectMsgPF(100 millis) {
        case AuthReadyMsg() ⇒ true
      }

      // auth by dev account

      authRmiClientMock2 ! AuthenticateMsg(authenticate)

      expectMsgPF(1000 millis) {
        case AuthenticationSuccessMsg(state) ⇒
          state.hasGame should be(true)
          true
      }

      // create client account rmi

      val accountRmiClientMock2 = system.actorOf(Props(classOf[AccountRMIClientMock], tcpClientMock2, testActor), "account-rmi-client-mock")

      clientReceiver2 ! RegisterReceiver(accountRmiClientMock2)

      expectMsgPF(100 millis) {
        case ReceiverRegistered(ref) ⇒ true
      }

      // create client enter game rmi

      val enterGameRmiClientMock2 = system.actorOf(Props(classOf[EnterGameRMIClientMock], tcpClientMock2, testActor), "enter-game-rmi-client-mock")

      clientReceiver2 ! RegisterReceiver(enterGameRmiClientMock2)

      expectMsgPF(100 millis) {
        case ReceiverRegistered(ref) ⇒ true
      }

      // join game

      enterGameRmiClientMock2 ! JoinMsg()

      expectMsgPF(1000 millis) {
        case JoinGameMsg(dto) ⇒ true
      }

      // create client game rmi

      val gameRmiClientMock2 = system.actorOf(Props(classOf[GameRMIClientMock], tcpClientMock2, testActor), "game-rmi-client-mock")

      clientReceiver2 ! RegisterReceiver(gameRmiClientMock2)

      expectMsgPF(100 millis) {
        case ReceiverRegistered(ref) ⇒ true
      }
    }
  }

  override protected def afterAll(): Unit = shutdown()
}
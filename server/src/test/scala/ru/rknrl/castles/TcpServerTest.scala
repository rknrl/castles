package ru.rknrl.castles

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.io.Tcp.Write
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import ru.rknrl.castles.rmi._
import ru.rknrl.core.rmi.testkit.{ClientConnected, ServerBounded, TcpClientMock, TcpMock}
import ru.rknrl.core.rmi.{RegisterReceiver, TcpReceiver}
import ru.rknrl.dto.AuthDTO._

import scala.concurrent.duration._

class TcpServerTest
  extends TestKit(ActorSystem("test-actor-system"))
  with WordSpecLike
  with DefaultTimeout
  with ImplicitSender
  with Matchers
  with BeforeAndAfterAll {

  import ru.rknrl.castles.CastlesTestSpec._

  val matchmaking = system.actorOf(Props(classOf[MatchmakingMock]), "matchmaking-mock")

  "TcpServer" should {
    "accept client connection" +
      "response with AuthReadyMsg" in {

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

      val authRmiClientMock = system.actorOf(Props(classOf[AuthRMIClientMock], tcpClientMock, testActor), "auth-rmi-client-mock")

      clientReceiver ! RegisterReceiver(authRmiClientMock, AuthRMIClientMock.allCommands)

      expectMsgPF(100 millis) {
        case AuthRMIClientMockReady() ⇒ true
      }

      expectMsgPF(100 millis) {
        case AuthReadyMsg() ⇒ true
      }

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
    }
  }

  override protected def afterAll(): Unit = shutdown()
}

class ByteArrayHandler(handler: ActorRef) extends Actor {
  override def receive = {
    case Write(data, _) ⇒
  }
}
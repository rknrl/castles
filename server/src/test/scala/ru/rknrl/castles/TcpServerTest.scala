package ru.rknrl.castles

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.io.Tcp.{Connected, Received, Register, Write}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import akka.util.ByteString
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

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
      val tcpServer = system.actorOf(Props(classOf[TcpServer], configMock, matchmaking), "test-tcpServer")
      tcpServer ! Connected(new InetSocketAddress("127.0.0.1", 123), new InetSocketAddress("127.0.0.1", 123))

      var tcpReceiver: Option[ActorRef] = None

      ignoreMsg {
        case Register(tcpRec, _, _) ⇒
          tcpReceiver = Some(tcpRec); true
      }

      expectMsgPF(100 millis) {
        case Write(data, _) ⇒ true // AuthReadyMsg
      }

      tcpReceiver.get ! Received(ByteString())
    }
  }

  override protected def afterAll(): Unit = shutdown()
}

class ByteArrayHandler(handler: ActorRef) extends Actor {
  override def receive = {
    case Write(data, _) ⇒
  }
}
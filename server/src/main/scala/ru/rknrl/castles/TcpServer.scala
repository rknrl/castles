package ru.rknrl.castles

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Props}
import akka.io.{IO, Tcp}
import ru.rknrl.core.rmi.TcpReceiver

// todo: tcp error handling
class TcpServer(config: Config, matchmaking: ActorRef) extends Actor {

  import akka.io.Tcp._
  import context.system

  val address = new InetSocketAddress(config.tcpIp, config.tcpPort)

  IO(Tcp) ! Bind(self, address)

  def receive = {
    case Bound(localAddress) ⇒
      println("bound " + localAddress)

    case CommandFailed(_: Bind) ⇒
      println("command failed " + address)
      context stop self

    case Connected(remote, local) ⇒
      val name = remote.getAddress.getHostAddress + ":" + remote.getPort
      val tcpSender = sender()
      val tcpReceiver = context.actorOf(Props(classOf[CastlesTcpReceiver], tcpSender, matchmaking, config, name), "tcp-receiver" + name)
      tcpSender ! Register(tcpReceiver)
  }
}

class CastlesTcpReceiver(tcpSender: ActorRef,
                         matchmaking: ActorRef,
                         config: Config,
                         name: String) extends TcpReceiver(name) {
  context.actorOf(Props(classOf[AuthService], tcpSender, self, matchmaking, config, name), "auth" + name)

  override def preStart(): Unit = println("TcpReceiver start " + name)

  override def postStop(): Unit = println("TcpReceiver stop " + name)
}
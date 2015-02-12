package ru.rknrl.castles

import java.net.InetSocketAddress

import akka.actor.{ActorLogging, ActorRef, Props}
import ru.rknrl.StoppingStrategyActor
import ru.rknrl.core.rmi.TcpReceiver

// todo: tcp error handling
class TcpServer(tcp: ActorRef, config: Config, matchmaking: ActorRef, accountStateDb: ActorRef) extends StoppingStrategyActor with ActorLogging {

  import akka.io.Tcp._

  val address = new InetSocketAddress(config.host, config.gamePort)

  tcp ! Bind(self, address)

  def receive = {
    case Bound(localAddress) ⇒
      log.info("bound " + localAddress)

    case CommandFailed(_: Bind) ⇒
      log.info("command failed " + address)
      context stop self

    case Connected(remote, local) ⇒
      val name = remote.getAddress.getHostAddress + ":" + remote.getPort
      val tcpSender = sender()
      val tcpReceiver = context.actorOf(Props(classOf[CastlesTcpReceiver], tcpSender, matchmaking, accountStateDb, config, name), "tcp-receiver" + name)
      tcpSender ! Register(tcpReceiver)
  }
}

class CastlesTcpReceiver(tcpSender: ActorRef,
                         matchmaking: ActorRef,
                         accountStateDb: ActorRef,
                         config: Config,
                         name: String) extends TcpReceiver(name) with ActorLogging {

  context.actorOf(Props(classOf[Auth], tcpSender, self, matchmaking, accountStateDb, config, name), "auth" + name)
}
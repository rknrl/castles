package ru.rknrl.castles

import java.net.InetSocketAddress

import akka.actor.{ActorLogging, ActorRef, Props}
import ru.rknrl.StoppingStrategyActor
import ru.rknrl.castles.account.Account
import ru.rknrl.rmi.Client

class TcpServer(tcp: ActorRef, config: Config, matchmaking: ActorRef, database: ActorRef) extends StoppingStrategyActor with ActorLogging {

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
      val client = context.actorOf(Props(classOf[CastlesClient], sender, matchmaking, database, config, name), "client" + name)
      sender ! Register(client)
  }
}
package ru.rknrl.castles.admin

import java.net.InetSocketAddress

import akka.actor.{ActorLogging, ActorRef, Props}
import ru.rknrl.StoppingStrategyActor

class AdminTcpServer(tcp: ActorRef,
                     host: String,
                     port: Int,
                     login: String,
                     password: String,
                     database: ActorRef,
                     matchmaking: ActorRef) extends StoppingStrategyActor with ActorLogging {

  import akka.io.Tcp._

  val address = new InetSocketAddress(host, port)

  tcp ! Bind(self, address)

  def receive = {
    case Bound(localAddress) ⇒
      log.info("bound " + localAddress)

    case CommandFailed(_: Bind) ⇒
      log.info("command failed " + address)
      context stop self

    case Connected(remote, local) ⇒
      val name = remote.getAddress.getHostAddress + ":" + remote.getPort
      val client = context.actorOf(Props(classOf[AdminClientSession], sender, database, matchmaking, login, password, name), "admin-client" + name)
      sender ! Register(client)
  }
}

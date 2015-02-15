//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles

import java.net.InetSocketAddress

import akka.actor.{ActorLogging, ActorRef, Props}
import ru.rknrl.StoppingStrategyActor
import ru.rknrl.castles.account.{AccountClientSession, Account}
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
      log.debug("connected " + name)
      val client = context.actorOf(Props(classOf[AccountClientSession], sender, matchmaking, database, config, name), "client" + name)
      sender ! Register(client)
  }
}
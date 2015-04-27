//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Props}
import ru.rknrl.Supervisor._
import ru.rknrl.castles.account.AccountClientSession
import ru.rknrl.logging.MiniLog

class TcpServer(tcp: ActorRef, config: Config, matchmaking: ActorRef, database: ActorRef, graphite: ActorRef, bugs: ActorRef, secretChecker: ActorRef) extends Actor {

  import akka.io.Tcp._

  override def supervisorStrategy = StopStrategy

  val log = new MiniLog

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
      val client = context.actorOf(Props(classOf[AccountClientSession], sender, matchmaking, secretChecker, database, graphite, bugs, config, name), "client" + name)
      sender ! Register(client)
  }
}
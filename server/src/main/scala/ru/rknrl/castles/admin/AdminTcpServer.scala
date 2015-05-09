//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.admin

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Props}
import ru.rknrl.Supervisor._
import ru.rknrl.castles.Config
import ru.rknrl.logging.ActorLog

class AdminTcpServer(tcp: ActorRef,
                     config: Config,
                     database: ActorRef,
                     matchmaking: ActorRef) extends Actor with ActorLog {

  import akka.io.Tcp._

  override def supervisorStrategy = StopStrategy

  val address = new InetSocketAddress(config.host, config.adminPort)

  tcp ! Bind(self, address)

  def receive = {
    case Bound(localAddress) ⇒
      log.info("bound " + localAddress)

    case CommandFailed(_: Bind) ⇒
      log.info("command failed " + address)
      context stop self

    case Connected(remote, local) ⇒
      val name = remote.getAddress.getHostAddress + ":" + remote.getPort
      val client = context.actorOf(Props(classOf[AdminClientSession], sender, database, matchmaking, config, name), "admin-client" + name.replace('.', '-'))
      sender ! Register(client)
  }
}

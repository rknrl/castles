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
import ru.rknrl.logging.MiniLog

class AdminTcpServer(tcp: ActorRef,
                     host: String,
                     port: Int,
                     login: String,
                     password: String,
                     database: ActorRef,
                     matchmaking: ActorRef,
                     bugs: ActorRef) extends Actor {

  import akka.io.Tcp._

  override def supervisorStrategy = StopStrategy

  val log = new MiniLog

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
      val client = context.actorOf(Props(classOf[AdminClientSession], sender, database, matchmaking, bugs, login, password, name), "admin-client" + name)
      sender ! Register(client)
  }
}

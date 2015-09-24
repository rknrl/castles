//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.core.rmi

import java.net.InetSocketAddress

import akka.actor.ActorRef
import akka.io.{IO, Tcp}

abstract class TcpClientConnection(host: String, port: Int) extends TcpClientSession(null) {

  import Tcp._
  import context.system

  val handler: ActorRef

  IO(Tcp) ! Connect(new InetSocketAddress(host, port))

  override def receive = {
    case c@Connected(remote, local) ⇒
      sender ! Register(self)
      handler ! c

      tcpSender = sender
      context become super.receive
  }
}
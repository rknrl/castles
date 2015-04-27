//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.core

import java.net.InetSocketAddress

import akka.actor.Actor
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.ByteString
import ru.rknrl.core.Graphite.Health
import ru.rknrl.dto.StatAction
import ru.rknrl.logging.MiniLog

object Graphite {

  case class Health(online: Int, games: Int)

}

class Graphite(host: String, port: Int) extends Actor {
  val log = new MiniLog(verbose = true)

  import context.system

  IO(Tcp) ! Connect(new InetSocketAddress(host, port))

  def receive = {
    case CommandFailed(_: Connect) ⇒
      log.debug("connect failed")
      context stop self

    case c@Connected(remote, local) ⇒
      log.debug("connected")
      val connection = sender
      connection ! Register(self)

      context become {
        case CommandFailed(w: Write) ⇒
          log.debug("write failed")

        case _: ConnectionClosed ⇒
          log.debug("connection closed")

        case Health(online, games) ⇒
          val path = "dev"
          connection ! Write(ByteString(path + ".totalmem" + " " + totalMem + " " + currentTime + "\n"))
          connection ! Write(ByteString(path + ".online" + " " + online + " " + currentTime + "\n"))
          connection ! Write(ByteString(path + ".games" + " " + games + " " + currentTime + "\n"))

        case a: StatAction ⇒
          val path = "castles"
          connection ! Write(ByteString(path + "." + a.name + ".count" + " 1 " + currentTime + "\n"))
      }
  }

  def totalMem = Runtime.getRuntime.totalMemory.toString

  def currentTime = (System.currentTimeMillis / 1000).toString
}

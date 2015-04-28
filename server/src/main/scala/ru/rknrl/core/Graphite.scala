//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.core

import java.net.InetSocketAddress

import akka.actor.{Actor, Props}
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
  val graphite = context.actorOf(Props(classOf[GraphiteConnection], host, port))
  val aggregator = context.actorOf(Props(classOf[GraphiteConnection], host, 2023))

  def receive = {
    case Health(online, games) ⇒
      graphite ! message("totalmem", Runtime.getRuntime.totalMemory)
      graphite ! message("online", online)
      graphite ! message("games", games)

    case a: StatAction ⇒
      aggregator ! message(a.name + "_sum", 1)
  }

  def message(name: String, value: Long) = Write(ByteString("dev." + name + " " + value + " " + currentTime + "\n"))

  def currentTime = (System.currentTimeMillis / 1000).toString
}

class GraphiteConnection(host: String, port: Int) extends Actor {
  val log = new MiniLog(verbose = true)

  import context.system

  IO(Tcp) ! Connect(new InetSocketAddress(host, port))

  def receive = {
    case CommandFailed(_: Connect) ⇒
      log.debug("connect failed " + port)
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

        case w: Write ⇒
          connection ! w
      }
  }
}

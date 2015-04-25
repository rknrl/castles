//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castlesbot

import akka.actor.{ActorSystem, Props}

object CastlesBotMain {
  val host = "127.0.0.1"
  val port = 2335

  def main(args: Array[String]) {
    println("CASTLES BOT 25 apr 2015")

    implicit val system = ActorSystem("main-actor-system")
    system.actorOf(Props(classOf[CastlesBotConnection], host, port))
  }
}

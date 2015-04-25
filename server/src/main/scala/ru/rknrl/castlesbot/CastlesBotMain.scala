//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castlesbot

import akka.actor.{ActorSystem, Props}
import ru.rknrl.dto.AccountId
import ru.rknrl.dto.AccountType.DEV

object CastlesBotMain {
  val host = "127.0.0.1"
  val port = 2335
  val count = 2

  def main(args: Array[String]) {
    println("CASTLES BOT 25 apr 2015")

    implicit val system = ActorSystem("main-actor-system")
    for (i ← 1 to count)
      system.actorOf(Props(classOf[CastlesBotConnection], host, port, AccountId(DEV, i.toString)))
  }
}

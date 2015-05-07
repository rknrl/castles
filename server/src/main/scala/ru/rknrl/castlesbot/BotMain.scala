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

object BotMain {
  val host = "localhost"
//  val host = "dev.rknrl.ru"
  val port = 2335
  val count = 100

  def main(args: Array[String]) {
    println("CASTLES BOT 3 may 2015")

    implicit val system = ActorSystem("main-actor-system")
    for (i ← 1 to count)
      system.actorOf(Props(classOf[BotConnection], host, port, AccountId(DEV, i.toString)), "bot-connection-" + i)
  }
}

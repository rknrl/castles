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
import ru.rknrl.logging.Bugs

object BotMain {
  val host = "dev.rknrl.ru"
  val port = 2335
  val count = 100

  def main(args: Array[String]) {
    println("CASTLES BOT 3 may 2015")

    implicit val system = ActorSystem("main-actor-system")
    val bugs = system.actorOf(Props(classOf[Bugs], "/Users/tolyayanot/bugs/", system.deadLetters), "bugs")
    for (i ← 1 to count)
      system.actorOf(Props(classOf[BotConnection], host, port, AccountId(DEV, i.toString), bugs))
  }
}

//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castlesbot

import akka.actor.ActorSystem
import generated.Serializer
import protos.AccountId
import protos.AccountType.DEV
import ru.rknrl.rpc.Connection

object BotMain {
  val host = "localhost"
  //  val host = "dev.rknrl.ru"
  val port = 2335
  val count = 100

  def main(args: Array[String]) {
    println("CASTLES BOT 3 may 2015")

    implicit val system = ActorSystem("main-actor-system")
    for (i ← 1 to count)
      system.actorOf(Connection.props(host, port, server ⇒ Bot.props(server, AccountId(DEV, i.toString)), new Serializer), "bot-connection-" + i)
  }
}

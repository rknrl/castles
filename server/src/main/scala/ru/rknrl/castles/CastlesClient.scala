//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles

import akka.actor.{ActorRef, Props}
import ru.rknrl.castles.account.Account
import ru.rknrl.rmi.Client

class CastlesClient(tcpSender: ActorRef,
                    matchmaking: ActorRef,
                    database: ActorRef,
                    config: Config,
                    name: String) extends Client(tcpSender, name) {

  val handler = context.actorOf(Props(classOf[Account], matchmaking, database, config, name), "account" + name)
}

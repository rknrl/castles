//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account

import akka.actor.{ActorRef, Props}
import ru.rknrl.castles.Config
import ru.rknrl.rmi.Client

class AccountClientSession(tcpSender: ActorRef,
                           matchmaking: ActorRef,
                           database: ActorRef,
                           config: Config,
                           name: String) extends Client(tcpSender, name) {

  val handler = context.actorOf(Props(classOf[Account], matchmaking, database, config, name), "account" + name)
}

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
                           secretChecker: ActorRef,
                           database: ActorRef,
                           graphite: ActorRef,
                           bugs: ActorRef,
                           config: Config,
                           name: String) extends Client(tcpSender, name) {

  val handler = context.actorOf(Props(classOf[Account], matchmaking, secretChecker, database, graphite, bugs, config), "account" + name)
}

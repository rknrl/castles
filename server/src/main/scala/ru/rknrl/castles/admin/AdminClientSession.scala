//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.admin

import akka.actor.{ActorRef, Props}
import ru.rknrl.castles.Config
import ru.rknrl.rmi.Client

class AdminClientSession(tcpSender: ActorRef,
                         database: ActorRef,
                         matchmaking: ActorRef,
                         config: Config,
                         name: String) extends Client(tcpSender, name) {
  val handler = context.actorOf(Props(classOf[Admin], database, matchmaking, config, name), "admin" + name.replace('.', '-'))
}

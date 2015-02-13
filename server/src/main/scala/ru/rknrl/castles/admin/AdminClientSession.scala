//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.admin

import akka.actor.{ActorRef, Props}
import ru.rknrl.rmi.Client

class AdminClientSession(tcpSender: ActorRef,
                         database: ActorRef,
                         matchmaking: ActorRef,
                         login: String,
                         password: String,
                         name: String) extends Client(tcpSender, name) {
  val handler = context.actorOf(Props(classOf[Admin], database, matchmaking, login, password, name), "admin" + name)
}

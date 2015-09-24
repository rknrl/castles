//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.admin

import akka.actor.ActorRef
import ru.rknrl.castles.Config
import ru.rknrl.rmi.Client

class AdminClientSession(tcpSender: ActorRef,
                         databaseQueue: ActorRef,
                         matchmaking: ActorRef,
                         config: Config,
                         name: String) extends Client(tcpSender) {
  val handler = context.actorOf(
    Admin.props(
      databaseQueue = databaseQueue,
      matchmaking = matchmaking,
      config = config
    ),
    "admin" + name.replace('.', '-')
  )
}

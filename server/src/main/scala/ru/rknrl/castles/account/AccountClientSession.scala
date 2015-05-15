//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account

import akka.actor.ActorRef
import ru.rknrl.castles.Config
import ru.rknrl.rmi.Client

class AccountClientSession(tcpSender: ActorRef,
                           matchmaking: ActorRef,
                           secretChecker: ActorRef,
                           databaseQueue: ActorRef,
                           graphite: ActorRef,
                           config: Config,
                           name: String) extends Client(tcpSender, name) {

  val handler = context.actorOf(
    Account.props(
      matchmaking = matchmaking,
      secretChecker = secretChecker,
      databaseQueue = databaseQueue,
      graphite = graphite,
      config = config
    ),
    "account-" + name.replace('.', '-')
  )
}

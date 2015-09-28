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
import ru.rknrl.castles.database.DatabaseTransaction.Calendar
import ru.rknrl.rmi.Client

object AccountClientSession {
  def props(tcp: ActorRef,
            matchmaking: ActorRef,
            secretChecker: ActorRef,
            databaseQueue: ActorRef,
            graphite: ActorRef,
            config: Config,
            calendar: Calendar,
            name: String) =
    Props(
      classOf[AccountClientSession],
      tcp,
      matchmaking,
      secretChecker,
      databaseQueue,
      graphite,
      config,
      calendar,
      name
    )
}

class AccountClientSession(tcp: ActorRef,
                           matchmaking: ActorRef,
                           secretChecker: ActorRef,
                           databaseQueue: ActorRef,
                           graphite: ActorRef,
                           config: Config,
                           calendar: Calendar,
                           name: String) extends Client(tcp) {

  val handler = context.actorOf(
    Account.props(
      matchmaking = matchmaking,
      secretChecker = secretChecker,
      databaseQueue = databaseQueue,
      graphite = graphite,
      config = config,
      calendar = calendar
    ),
    "account-" + name.replace('.', '-')
  )
}

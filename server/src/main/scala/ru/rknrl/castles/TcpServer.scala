//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Props}
import ru.rknrl.Supervisor._
import ru.rknrl.castles.account.AccountClientSession
import ru.rknrl.castles.database.DatabaseTransaction.Calendar
import ru.rknrl.logging.ActorLog

object TcpServer {
  def props(tcp: ActorRef,
            config: Config,
            matchmaking: ActorRef,
            databaseQueue: ActorRef,
            graphite: ActorRef,
            secretChecker: ActorRef,
            calendar: Calendar) =
    Props(
      classOf[TcpServer],
      tcp,
      config,
      matchmaking,
      databaseQueue,
      graphite,
      secretChecker,
      calendar
    )
}

class TcpServer(tcp: ActorRef,
                config: Config,
                matchmaking: ActorRef,
                databaseQueue: ActorRef,
                graphite: ActorRef,
                secretChecker: ActorRef,
                calendar: Calendar) extends Actor with ActorLog {

  import akka.io.Tcp._

  override def supervisorStrategy = StopStrategy

  val address = new InetSocketAddress(config.host, config.gamePort)

  tcp ! Bind(self, address)

  def receive = {
    case Bound(localAddress) ⇒
      log.info("bound " + localAddress)

    case CommandFailed(_: Bind) ⇒
      log.info("command failed " + address)
      context stop self

    case Connected(remote, local) ⇒
      val name = remote.getAddress.getHostAddress + ":" + remote.getPort
      log.debug("connected " + name)
      val client = context.actorOf(
        AccountClientSession.props(
          tcpSender = sender,
          matchmaking = matchmaking,
          secretChecker = secretChecker,
          databaseQueue = databaseQueue,
          graphite = graphite,
          config = config,
          calendar = calendar,
          name = name
        ),
        "client-" + name.replace('.', '-')
      )
      sender ! Register(client)
  }
}
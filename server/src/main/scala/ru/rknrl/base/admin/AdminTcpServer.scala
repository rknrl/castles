package ru.rknrl.base.admin

import java.net.InetSocketAddress

import akka.actor.{ActorLogging, ActorRef, Props}
import ru.rknrl.castles.rmi._
import ru.rknrl.core.rmi._
import ru.rknrl.{EscalateStrategyActor, StoppingStrategyActor}

class AdminTcpServer(tcp: ActorRef,
                     host: String,
                     port: Int,
                     login: String,
                     password: String,
                     accountStateDb: ActorRef,
                     matchmaking: ActorRef) extends StoppingStrategyActor with ActorLogging {

  import akka.io.Tcp._

  val address = new InetSocketAddress(host, port)

  tcp ! Bind(self, address)

  def receive = {
    case Bound(localAddress) ⇒
      log.info("bound " + localAddress)

    case CommandFailed(_: Bind) ⇒
      log.info("command failed " + address)
      context stop self

    case Connected(remote, local) ⇒
      val name = remote.getAddress.getHostAddress + ":" + remote.getPort
      val tcpSender = sender()
      val tcpReceiver = context.actorOf(Props(classOf[AuthTcpReceiver], tcpSender, accountStateDb, matchmaking, login, password, name), "admin-tcp-receiver" + name)
      tcpSender ! Register(tcpReceiver)
  }
}

class AuthTcpReceiver(tcpSender: ActorRef,
                      accountStateDb: ActorRef,
                      matchmaking: ActorRef,
                      login: String,
                      password: String,
                      name: String) extends TcpReceiver(name) with ActorLogging {

  context.actorOf(Props(classOf[AdminAuth], tcpSender, self, accountStateDb, matchmaking, login, password, name), "admin-auth" + name)
}

class AdminAuth(tcpSender: ActorRef,
                tcpReceiver: ActorRef,
                accountStateDb: ActorRef,
                matchmaking: ActorRef,
                login: String,
                password: String,
                name: String) extends EscalateStrategyActor with ActorLogging {

  private val rmi = context.actorOf(Props(classOf[AdminAuthRMI], tcpSender, self), "admin-auth-rmi" + name)
  tcpReceiver ! RegisterReceiver(rmi)

  def receive = {
    case ReceiverRegistered(ref) ⇒
      rmi ! AdminAuthReadyMsg()

    /** from player */
    case AuthenticateAsAdminMsg(authenticate) ⇒
      if (authenticate.getLogin == login && authenticate.getPassword == password) {
        context.actorOf(Props(classOf[Admin], tcpSender, tcpReceiver, accountStateDb, matchmaking, name), "admin" + name)
        sender ! AuthenticatedAsAdminMsg()
      } else {
        log.info("reject")
        tcpReceiver ! UnregisterReceiver(rmi)
        tcpReceiver ! CloseConnection
      }
  }
}


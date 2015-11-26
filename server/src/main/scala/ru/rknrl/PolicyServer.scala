//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl

import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets

import akka.actor._
import akka.io.Tcp._
import akka.util.ByteString
import ru.rknrl.Supervisor.StopStrategy
import ru.rknrl.logging.ActorLog

object PolicyServer {
  def props(tcp: ActorRef, host: String, port: Int) =
    Props(classOf[PolicyServer], tcp, host, port)
}

class PolicyServer(tcp: ActorRef, host: String, port: Int) extends Actor with ActorLog {

  override def supervisorStrategy = StopStrategy

  val address = new InetSocketAddress(host, port)

  tcp ! Bind(self, address)

  def receive = {
    case Bound(localAddress) ⇒
      log.info("policy server bound " + localAddress)

    case CommandFailed(_: Bind) ⇒
      log.info("policy server command failed " + address)
      context stop self

    case Connected(remote, local) ⇒
      val name = remote.getAddress.getHostAddress + ":" + remote.getPort
      log.info("policy peer connected " + name)

      val tcpReceiver = context.actorOf(Props(classOf[PolicyReceiver], name), "policy-receiver" + name)
      sender ! Register(tcpReceiver)
  }
}

class PolicyReceiver(name: String) extends Actor with ActorLog {

  import akka.io.Tcp._

  import context.dispatcher
  import scala.concurrent.duration._

  private val policyResponseText =
    "<?xml version=\"1.0\"?>" +
      "<!DOCTYPE cross-domain-policy SYSTEM \"/xml/dtds/cross-domain-policy.dtd\">" +
      "<cross-domain-policy>" +
      "<allow-access-from domain=\"*\" to-ports=\"*\" />" +
      "</cross-domain-policy>\0"

  private val policyResponse = Write(ByteString(policyResponseText.getBytes(StandardCharsets.US_ASCII.toString)))

  context.system.scheduler.scheduleOnce(1 minute, self, PoisonPill)

  override final def receive = {
    case Received(receivedData) ⇒
      sender ! policyResponse

    case PeerClosed ⇒
      log.info("policy peer closed " + name)
      context stop self
  }
}


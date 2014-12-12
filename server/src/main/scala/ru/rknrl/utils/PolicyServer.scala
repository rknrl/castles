package ru.rknrl.utils

import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets

import akka.actor.{Actor, ActorRef, Props}
import akka.util.ByteString

class PolicyServer(tcp: ActorRef, host: String, port: Int) extends Actor {

  import akka.io.Tcp._

  val address = new InetSocketAddress(host, port)

  tcp ! Bind(self, address)

  def receive = {
    case Bound(localAddress) ⇒
      println("policy server bound " + localAddress)

    case CommandFailed(_: Bind) ⇒
      println("policy server command failed " + address)
      context stop self

    case Connected(remote, local) ⇒
      val name = remote.getAddress.getHostAddress + ":" + remote.getPort
      println("policy peer connected " + name)

      val tcpReceiver = context.actorOf(Props(classOf[PolicyReceiver], name), "policy-receiver" + name)
      sender() ! Register(tcpReceiver)
  }
}

class PolicyReceiver(name: String) extends Actor {

  import akka.io.Tcp._

  private val policyResponseText =
    "<?xml version=\"1.0\"?>" +
      "<!DOCTYPE cross-domain-policy SYSTEM \"/xml/dtds/cross-domain-policy.dtd\">" +
      "<cross-domain-policy>" +
      "<allow-access-from domain=\"*\" to-ports=\"*\" />" +
      "</cross-domain-policy>\0"

  private val policyResponse = Write(ByteString(policyResponseText.getBytes(StandardCharsets.US_ASCII.toString)))

  override final def receive = {
    case Received(receivedData) ⇒
      sender() ! policyResponse

    case PeerClosed ⇒
      println("policy peer closed " + name)
      context stop self
  }
}


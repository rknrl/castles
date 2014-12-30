package ru.rknrl.castles

import akka.actor.{ActorSystem, Props}
import akka.io.{IO, Tcp}
import ru.rknrl.castles.config.Config
import ru.rknrl.castles.config.ConfigJsonProtocol._
import ru.rknrl.castles.database.InMemoryDb
import ru.rknrl.castles.payments.PaymentsServer
import ru.rknrl.utils.PolicyServer
import spray.can.Http
import spray.json._

import scala.concurrent.duration._
import scala.io.Source

object Main {

  def main(args: Array[String]): Unit = {
    println("Version 0.02")

    val configPath = "/Users/tolyayanot/dev/rknrl/castles/server/src/main/resources/dev.json"

    val configString = Source.fromFile(configPath).mkString

    val config = configString.parseJson.convertTo[Config]

    implicit val system = ActorSystem("main-actor-system")

    val matchmaking = system.actorOf(Props(classOf[MatchMaking], 3 seconds, config.game), "matchmaking")

    val payments = system.actorOf(Props(classOf[PaymentsServer], config), "payment-server")
    IO(Http) ! Http.Bind(payments, config.host, 8080)

    val accountStateDb = system.actorOf(Props(classOf[InMemoryDb]), "account-state-db")

    val tcp = IO(Tcp)
    system.actorOf(Props(classOf[PolicyServer], tcp, config.host, config.policyPort), "policy-server")
    system.actorOf(Props(classOf[TcpServer], tcp, config, matchmaking, accountStateDb), "tcp-server")
  }
}
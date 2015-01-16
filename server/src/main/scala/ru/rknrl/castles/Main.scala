package ru.rknrl.castles

import akka.actor.{ActorSystem, Props}
import akka.io.{IO, Tcp}
import net.liftweb.json._
import ru.rknrl.castles.database.InMemoryDb
import ru.rknrl.castles.payments.PaymentsServer
import ru.rknrl.utils.PolicyServer
import spray.can.Http

import scala.concurrent.duration._
import scala.io.Source

object Main {
  implicit val formats = DefaultFormats + new BuildingPricesSerializer + new BuildingLevelToFactorSerializer + new BuildingsConfigSerializer + new SkillUpgradePricesSerializer

  def main(args: Array[String]): Unit = {
    val configPath = args(0)

    println(s"ver 0.05")
    println(s"configPath='$configPath'")

    val configString = Source.fromFile(configPath, "UTF-8").mkString

    val config = JsonParser.parse(configString).extract[Config]

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
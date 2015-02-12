package ru.rknrl.castles

import akka.actor.{ActorSystem, Props}
import akka.io.{IO, Tcp}
import akka.pattern._
import net.liftweb.json._
import ru.rknrl.PolicyServer
import ru.rknrl.castles.admin.AdminTcpServer
import ru.rknrl.castles.database.AccountStateDb.GetTop
import ru.rknrl.castles.database.MySqlDb
import ru.rknrl.castles.payments.PaymentsServer
import spray.can.Http

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.Source

object Main {
  implicit val formats = DefaultFormats + new BuildingPricesSerializer + new BuildingLevelToFactorSerializer + new BuildingsConfigSerializer + new SkillUpgradePricesSerializer

  def main(configPaths: Array[String]): Unit = {
    println(s"ver: 29 jan 2015 4:28")
    configPaths.map(path ⇒ println(s"configPath='$path'"))

    val configStrings = configPaths.map(path ⇒ Source.fromFile(path, "UTF-8").mkString)
    val parsedConfigs = configStrings.map(JsonParser.parse)
    val iterator = parsedConfigs.iterator
    var mergedConfig = iterator.next()
    while (iterator.hasNext) mergedConfig = mergedConfig merge iterator.next()
    val config = mergedConfig.extract[Config]

    implicit val system = ActorSystem("main-actor-system")

    val accountStateDb = system.actorOf(Props(classOf[MySqlDb], config.db), "account-state-db")
    val future = Patterns.ask(accountStateDb, GetTop, 5 seconds)
    val top = Await.result(future, 5 seconds)

    val matchmaking = system.actorOf(Props(classOf[MatchMaking], 3 seconds, top, config.game), "matchmaking")

    val payments = system.actorOf(Props(classOf[PaymentsServer], config, matchmaking), "payment-server")
    IO(Http) ! Http.Bind(payments, config.host, config.httpPort)

    val tcp = IO(Tcp)
    system.actorOf(Props(classOf[PolicyServer], tcp, config.host, config.policyPort), "policy-server")
    system.actorOf(Props(classOf[AdminTcpServer], tcp, config.host, config.adminPort, config.adminLogin, config.adminPassword, accountStateDb, matchmaking), "admin-server")
    system.actorOf(Props(classOf[TcpServer], tcp, config, matchmaking, accountStateDb), "tcp-server")
  }
}
package ru.rknrl.castles

import akka.actor.{ActorSystem, Props}
import akka.io.{IO, Tcp}
import akka.util.Timeout
import net.liftweb.json._
import ru.rknrl.PolicyServer
import ru.rknrl.base.MatchMaking.TopItem
import ru.rknrl.base.TcpServer
import ru.rknrl.base.payments.PaymentsServer
import ru.rknrl.castles.database.AccountStateDb.{Put, GetTop}
import ru.rknrl.castles.database.MySqlDb
import spray.can.Http

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.io.Source
import akka.pattern._

object Main {
  implicit val formats = DefaultFormats + new BuildingPricesSerializer + new BuildingLevelToFactorSerializer + new BuildingsConfigSerializer + new SkillUpgradePricesSerializer

  def main(configPaths: Array[String]): Unit = {
    println(s"ver 0.07")
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

    val matchmaking = system.actorOf(Props(classOf[CastlesMatchMaking], 3 seconds, top, config.game), "matchmaking")

    val payments = system.actorOf(Props(classOf[PaymentsServer], config), "payment-server")
    IO(Http) ! Http.Bind(payments, config.host, 8080)

    val tcp = IO(Tcp)
    system.actorOf(Props(classOf[PolicyServer], tcp, config.host, config.policyPort), "policy-server")
    system.actorOf(Props(classOf[TcpServer], tcp, config, matchmaking, accountStateDb), "tcp-server")
  }
}
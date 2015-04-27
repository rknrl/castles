//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles

import akka.actor.{ActorSystem, Props}
import akka.io.{IO, Tcp}
import akka.pattern._
import net.liftweb.json._
import ru.rknrl.PolicyServer
import ru.rknrl.castles.account.SecretChecker
import ru.rknrl.castles.admin.AdminTcpServer
import ru.rknrl.castles.database.Database
import ru.rknrl.castles.database.Database.GetTop
import ru.rknrl.castles.game.init.GameMaps
import ru.rknrl.castles.matchmaking.{GameCreator, GameFactory, MatchMaking}
import ru.rknrl.castles.payments.HttpServer
import ru.rknrl.core.Graphite
import ru.rknrl.logging.Bugs
import spray.can.Http

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.Source

object Main {
  implicit val formats = DefaultFormats + new BuildingPricesSerializer + new SkillUpgradePricesSerializer

  def main(configPaths: Array[String]): Unit = {
    println(s"ver: 18 apr 2015 00:34")
    configPaths.foreach(path ⇒ println(s"configPath='$path'"))

    val configStrings = configPaths.map(path ⇒ Source.fromFile(path, "UTF-8").mkString)
    val parsedConfigs = configStrings.map(JsonParser.parse)
    val iterator = parsedConfigs.iterator
    var mergedConfig = iterator.next()
    while (iterator.hasNext) mergedConfig = mergedConfig merge iterator.next()
    val config = mergedConfig.extract[Config]

    val gameMaps = GameMaps.fromFiles(config.mapsDir)

    implicit val system = ActorSystem("main-actor-system")

    val bugs = system.actorOf(Props(classOf[Bugs], config.bugsDir), "bugs")
    val secretChecker = system.actorOf(Props(classOf[SecretChecker], config), "secret-checker")

    val graphite = system.actorOf(Props(classOf[Graphite], config.graphiteHost, config.graphitePort), "graphite")
    val database = system.actorOf(Props(classOf[Database], config.db, bugs), "database")
    val future = Patterns.ask(database, GetTop, 5 seconds)
    val top = Await.result(future, 5 seconds)

    val gameCreator = new GameCreator(gameMaps, config)
    val matchmaking = system.actorOf(Props(classOf[MatchMaking], gameCreator, new GameFactory(), 7 seconds, top, config, database, graphite, bugs), "matchmaking")

    val payments = system.actorOf(Props(classOf[HttpServer], config, database, matchmaking, bugs), "http-server")
    IO(Http) ! Http.Bind(payments, config.host, config.httpPort)

    val tcp = IO(Tcp)
    system.actorOf(Props(classOf[PolicyServer], tcp, config.host, config.policyPort), "policy-server")
    system.actorOf(Props(classOf[AdminTcpServer], tcp, config.host, config.adminPort, config.adminLogin, config.adminPassword, database, matchmaking, bugs), "admin-server")
    system.actorOf(Props(classOf[TcpServer], tcp, config, matchmaking, database, graphite, bugs, secretChecker), "tcp-server")
  }
}
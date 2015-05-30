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
import net.liftweb.json._
import ru.rknrl.PolicyServer
import ru.rknrl.castles.account.SecretChecker
import ru.rknrl.castles.admin.AdminTcpServer
import ru.rknrl.castles.database.{Database, DatabaseCache, DatabaseQueue, DatabaseTransaction}
import ru.rknrl.castles.game.init.GameMaps
import ru.rknrl.castles.matchmaking.{GameCreator, GameFactory, MatchMaking}
import ru.rknrl.castles.payments.HttpServer
import ru.rknrl.core.Graphite
import ru.rknrl.logging.Bugs
import spray.can.Http

import scala.concurrent.duration._
import scala.io.Source

object Main {
  implicit val formats = DefaultFormats + new BuildingPricesSerializer + new SkillUpgradePricesSerializer

  def main(configPaths: Array[String]): Unit = {
    println(s"ver: 7 may 2015 21:22")
    configPaths.foreach(path ⇒ println(s"configPath='$path'"))

    val configStrings = configPaths.map(path ⇒ Source.fromFile(path, "UTF-8").mkString)
    val parsedConfigs = configStrings.map(JsonParser.parse)
    val iterator = parsedConfigs.iterator
    var mergedConfig = iterator.next()
    while (iterator.hasNext) mergedConfig = mergedConfig merge iterator.next()
    val config = mergedConfig.extract[Config]

    val gameMaps = GameMaps.fromFiles(config.mapsDir)

    implicit val system = ActorSystem("main-actor-system")

    val graphite = system.actorOf(Graphite.props(config.graphite, config.isDev), "graphite")
    val secretChecker = system.actorOf(SecretChecker.props(config), "secret-checker")

    val database = system.actorOf(Database.props(config.db), "database")
    val databaseCache = system.actorOf(DatabaseCache.props(database), "database-cache")
    val databaseTransaction = system.actorOf(DatabaseTransaction.props(databaseCache), "database-transaction")
    val databaseQueue = system.actorOf(DatabaseQueue.props(databaseTransaction), "database-queue")

    val gameCreator = new GameCreator(gameMaps, config)
    val matchmaking = system.actorOf(
      MatchMaking.props(
        gameCreator = gameCreator,
        gameFactory = new GameFactory(),
        interval = 7 seconds,
        config = config,
        databaseQueue = databaseQueue,
        graphite = graphite
      ),
      "matchmaking"
    )

    val bugs = system.actorOf(Bugs.props(config.clientBugsDir), "bugs")
    val httpServer = system.actorOf(HttpServer.props(config, databaseQueue, matchmaking, bugs), "http-server")
    IO(Http) ! Http.Bind(httpServer, config.host, config.httpPort)

    val tcp = IO(Tcp)
    system.actorOf(PolicyServer.props(tcp, config.host, config.policyPort), "policy-server")
    system.actorOf(AdminTcpServer.props(tcp, config, databaseQueue, matchmaking), "admin-server")
    system.actorOf(TcpServer.props(tcp, config, matchmaking, databaseQueue, graphite, secretChecker), "tcp-server")
  }
}
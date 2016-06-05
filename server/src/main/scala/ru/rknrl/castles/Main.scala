//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles

import akka.actor.ActorSystem
import generated.Serializer
import net.liftweb.json.{DefaultFormats, JsonParser}
import rknrl.CrossDomainPolicyServer
import ru.rknrl.castles.account.{Account, SecretChecker}
import ru.rknrl.castles.admin.Admin
import ru.rknrl.castles.database.DatabaseTransaction.RealCalendar
import ru.rknrl.castles.database.{Database, DatabaseCache, DatabaseQueue, DatabaseTransaction}
import ru.rknrl.castles.game.init.GameMaps
import ru.rknrl.castles.matchmaking.{GameCreator, GameFactory, MatchMaking}
import ru.rknrl.castles.payments.HttpServer
import ru.rknrl.core.Graphite
import ru.rknrl.logging.Bugs
import ru.rknrl.rpc.Server

import scala.concurrent.duration._
import scala.io.Source

object Main {

  implicit val formats = DefaultFormats + new BuildingPricesSerializer + new SkillUpgradePricesSerializer

  def main(configPaths: Array[String]): Unit = {
    println(s"ver: 6 june 2015 22:49")
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
    val calendar = new RealCalendar
    val databaseTransaction = system.actorOf(DatabaseTransaction.props(databaseCache, calendar), "database-transaction")
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

    system.actorOf(HttpServer.props(config, databaseQueue, matchmaking, bugs), "http-server")

    system.actorOf(CrossDomainPolicyServer.props(config.host, config.policyPort), "policy-server")

    system.actorOf(Server.props(config.host, config.gamePort,
      client ⇒ Account.props(matchmaking, secretChecker, databaseQueue, graphite, config, calendar),
      Serializer),
      "client-server")

    system.actorOf(Server.props(config.host, config.adminPort,
      client ⇒ Admin.props(client, databaseQueue, matchmaking, config),
      Serializer),
      "admin-server")
  }
}
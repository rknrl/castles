package ru.rknrl.castles

import akka.actor.{ActorSystem, Props}
import ru.rknrl.castles.config.Config
import ru.rknrl.castles.config.ConfigJsonProtocol._
import ru.rknrl.castles.web.Web
import spray.json._

import scala.io.Source

object Main {
  def main(args: Array[String]): Unit = {
    val configPath = "/Users/tolyayanot/dev/rknrl/castles/server/src/main/resources/dev.json"
    //    val configPath = "/var/castles-server/dev.json"

    val configString = Source.fromFile(configPath).mkString

    val config = configString.parseJson.convertTo[Config]

    val system = ActorSystem("main-actor-system")

    val matchmaking = system.actorOf(Props(classOf[MatchMaking], config.game), "matchmaking")

    new Web(config)

    system.actorOf(Props(classOf[TcpServer], config, matchmaking), "tcp-server")
  }
}

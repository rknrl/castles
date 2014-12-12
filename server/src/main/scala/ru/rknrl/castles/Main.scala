package ru.rknrl.castles

import akka.actor.{ActorSystem, Props}
import akka.io.{IO, Tcp}
import ru.rknrl.castles.config.Config
import ru.rknrl.castles.config.ConfigJsonProtocol._
import ru.rknrl.castles.web.Web
import spray.json._

import scala.io.Source
import scala.concurrent.duration._

object Main {

  def main(args: Array[String]): Unit = {
    val configPath = "/Users/tolyayanot/dev/rknrl/castles/server/src/main/resources/dev.json"
    //    val configPath = "/var/castles-server/dev.json"

    val configString = Source.fromFile(configPath).mkString

    val config = configString.parseJson.convertTo[Config]

    implicit val system = ActorSystem("main-actor-system")

    val matchmaking = system.actorOf(Props(classOf[MatchMaking], 3 seconds, config.game), "matchmaking")

    new Web(config)

    system.actorOf(Props(classOf[TcpServer], IO(Tcp), config, matchmaking), "tcp-server")
  }
}

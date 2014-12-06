package ru.rknrl.castles

import akka.actor.{ActorSystem, Props}
import ru.rknrl.castles.web.Web
import ru.rknrl.core.social.SocialConfigs
import spray.json._

import scala.io.Source

object ConfigJsonProtocol extends DefaultJsonProtocol {

  import ru.rknrl.core.social.SocialConfigJsonProtocol._

  implicit object ConfigJsonFormat extends RootJsonFormat[Config] {
    def write(config: Config) =
      throw new RuntimeException("unsupported")

    def read(value: JsValue) = value match {
      case JsObject(map) ⇒
        val socialConfigs = map("social").convertTo[SocialConfigs]
        new Config(socialConfigs)

      case _ => deserializationError("Config")
    }
  }

}

import ru.rknrl.castles.ConfigJsonProtocol._

object Main {
  def main(args: Array[String]): Unit = {
//    val configPath = "/Users/tolyayanot/dev/rknrl/castles/server/src/main/resources/dev.json"
    val configPath = "/var/castles-server/dev.json"

    val configJson = Source.fromFile(configPath).mkString

    val config: Config = configJson.parseJson.convertTo[Config]

    val system = ActorSystem("main-actor-system")

    val matchmaking = system.actorOf(Props(classOf[MatchMaking], config.gameConfig), "matchmaking")

    new Web(config)

    system.actorOf(Props(classOf[TcpServer], config, matchmaking), "tcp-server")
  }
}

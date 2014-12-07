package ru.rknrl.castles.web

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import ru.rknrl.castles.config.Config
import spray.can.Http

class Web(config: Config) {
  implicit val system = ActorSystem("spray-actor-system")
  val actor = system.actorOf(Props(classOf[MmPayments]), "mm-payment-actor")
  IO(Http)(system) ! Http.Bind(actor, "localhost", 8090)
}




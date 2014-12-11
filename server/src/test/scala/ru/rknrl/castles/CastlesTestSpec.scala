package ru.rknrl.castles

import akka.actor.Actor
import ru.rknrl.castles.config.ConfigTest

object CastlesTestSpec {

  class TcpReceiverMock extends Actor {
    override def receive = {
      case _ ⇒
    }
  }

  class TcpSenderMock extends Actor {
    override def receive = {
      case _ ⇒
    }
  }

  class MatchmakingMock extends Actor {
    override def receive = {
      case _ ⇒
    }
  }

  val configMock = ConfigTest.configMock
}

//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.matchmaking

import akka.actor.{Actor, ActorRef, Props}
import ru.rknrl.castles.game.GameScheduler
import ru.rknrl.castles.kit.{Mocks, ActorsTest}

class GameFactoryTest extends ActorsTest {
  "create" in {
    val creator = system.actorOf(Props(classOf[Creator]))
    creator ! "create"
    expectMsgPF(timeout.duration) {
      case ref: ActorRef ⇒ ref.path.toString should endWith("game-0")
    }
    creator ! "create"
    expectMsgPF(timeout.duration) {
      case ref: ActorRef ⇒ ref.path.toString should endWith("game-1")
    }
  }
}

class Creator extends Actor {
  val factory = new GameFactory

  def receive = {
    case "create" ⇒ sender ! factory.create(
      gameState = Mocks.gameStateMock(),
      isDev = true,
      schedulerClass = classOf[GameScheduler],
      matchmaking = self,
      bugs = self
    )
  }
}
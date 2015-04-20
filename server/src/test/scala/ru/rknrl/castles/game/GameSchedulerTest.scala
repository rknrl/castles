//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game

import akka.actor.Props
import ru.rknrl.castles.game.Game.UpdateGameState
import ru.rknrl.castles.kit.ActorsTest

import scala.concurrent.duration._

class GameSchedulerTest extends ActorsTest {
  var iterator = 0
  multi("scheduler", {
    iterator += 1
    val scheduler = system.actorOf(Props(classOf[GameScheduler], self), "game-scheduler" + iterator)

    var newTime1 = 0L

    expectMsgPF(100 millis) {
      case UpdateGameState(newTime) ⇒ newTime1 = newTime
    }

    expectMsgPF(100 millis) {
      case UpdateGameState(newTime) ⇒ (newTime - newTime1) shouldBe (33L +- 33)
    }

    system stop scheduler
  })
}

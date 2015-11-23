//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game

import akka.actor.{Actor, ActorRef}
import ru.rknrl.castles.game.Game.UpdateGameState

import scala.concurrent.duration._

class GameScheduler(game: ActorRef) extends Actor {

  import context.dispatcher

  val sendFps = 30
  val sendInterval = 1000 / sendFps

  case object Tick

  val scheduler = context.system.scheduler.scheduleO(0 seconds, sendInterval milliseconds, self, Tick)

  def receive = {
    case Tick ⇒ game ! UpdateGameState(newTime = System.currentTimeMillis())
  }
}

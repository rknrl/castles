//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl

import akka.actor.SupervisorStrategy.{Escalate, Restart, Stop}
import akka.actor._

object Supervisor {
  val StopStrategy = OneForOneStrategy() {
    case _: Exception ⇒ Stop
  }

  val RestartStrategy = OneForOneStrategy() {
    case _: Exception ⇒ Restart
  }

  val EscalateStrategy = OneForOneStrategy() {
    case _: Exception ⇒ Escalate
  }
}
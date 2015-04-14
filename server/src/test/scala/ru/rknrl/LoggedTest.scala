//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl

import akka.actor.{Actor, ActorRef, Props}
import ru.rknrl.BugType.BugType
import ru.rknrl.Bugs.Bug
import ru.rknrl.castles.kit.ActorsTest

class LoggedTest extends ActorsTest {

  "with bugType & bugs" in {
    val actor = system.actorOf(Props(classOf[TestLoggedActor], Some(BugType.GAME), Some(self)))
    actor ! "ping"
    expectMsg("pong")
    actor ! "pong"
    expectMsg("ping")
    actor ! "log"
    expectMsg("handled ping\nhandled log\n")
    actor ! "exception"
    expectMsgPF(timeout.duration) {
      case Bug(BugType.GAME, log) ⇒
        log should startWith("handled ping\nhandled log\nhandled exception\ntestError\nru.rknrl.")
    }
  }

  "without bugType & bugs" in {
    val actor = system.actorOf(Props(classOf[TestLoggedActor], None, None))
    actor ! "ping"
    expectMsg("pong")
    actor ! "pong"
    expectMsg("ping")
    actor ! "log"
    expectMsg("handled ping\nhandled log\n")
    actor ! "exception"
    actor ! "ping"
    expectMsg("pong")
  }

}

class TestLoggedActor(bugType: Option[BugType], bugs: Option[ActorRef]) extends Actor {
  val log = new SilentLog

  def logged(r: Receive) = new Logged(r, log, bugs, bugType, {
    case "pong" ⇒ false
    case _ ⇒ true
  })

  def receive = logged({
    case "ping" ⇒ sender ! "pong"
    case "pong" ⇒ sender ! "ping"
    case "log" ⇒ sender ! log.result
    case "exception" ⇒ throw new Exception("testError")
  })
}
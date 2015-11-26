//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.logging

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorContext, ActorRef}
import org.slf4j.{Logger, LoggerFactory}

trait ActorLog { this: Actor ⇒
  val log = LoggerFactory.getLogger(self.path.name)

  val logFilter: Any ⇒ Boolean = any ⇒ true

  def logged(r: Receive) = new Logged(r, log, logFilter)

  def catched(r: Receive) = new Catched(r, log)

  def send(to: ActorRef, msg: Any): Unit = {
    log.debug("→ " + msg)
    to ! msg
  }

  def forward(to: ActorRef, msg: Any): Unit = {
    log.debug("→→ " + msg)
    to forward msg
  }

  def become(behavior: Receive, name: String): Unit = {
    log.debug("become " + name)
    context become behavior
  }
}

class Logged(r: Receive,
             log: Logger,
             filter: Any ⇒ Boolean) extends Catched(r, log) {

  override def isDefinedAt(o: Any): Boolean = {
    val handled = r.isDefinedAt(o)
    if (filter(o))
      log debug (if (handled) "✓" else "✖") + " " + o
    handled
  }
}

class Catched(r: Receive,
              log: Logger) extends Receive {

  def isDefinedAt(o: Any): Boolean = r.isDefinedAt(o)

  def apply(o: Any): Unit =
    try {
      r(o)
    } catch {
      case t: Throwable ⇒
        log.error(t.getMessage, t)
        throw t
    }
}
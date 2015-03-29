//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl

import akka.actor.Actor.Receive
import akka.actor.ActorContext
import org.slf4j.Logger

trait Log {
  def info(s: String)
}

class SilentLog extends Log {
  val builder = new StringBuilder

  def info(s: String) = {
    builder append s + "\n"
    println(s)
  }

  def result = builder.result
}

class Slf4j(val logger: Logger) extends Log {
  def info(s: String) = logger.info(s)
  def error(s: String) = logger.error(s)
  def debug(s: String) = logger.debug(s)
}

class Logged(r: Receive, log: Log, filter: Any ⇒ Boolean)(implicit context: ActorContext) extends Receive {
  def isDefinedAt(o: Any): Boolean = {
    val handled = r.isDefinedAt(o)
    if (filter(o))
      log info (if (handled) "handled" else "unhandled") + " " + o
    handled
  }

  def apply(o: Any): Unit = r(o)
}
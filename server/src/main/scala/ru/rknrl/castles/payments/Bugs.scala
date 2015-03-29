//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.payments

import java.io.PrintWriter

import akka.actor.Actor
import org.slf4j.LoggerFactory
import ru.rknrl.castles.payments.Bugs.Bug

object BugType extends Enumeration {
  type BugType = Value
  val CLIENT = Value
  val GAME = Value
  val BOT = Value
  val ACCOUNT = Value
}

import ru.rknrl.castles.payments.BugType._

class BugsConfig(clientDir: String,
                 gameDir: String,
                 botDir: String,
                 accountDir: String) {
  def dir(bugType: BugType) =
    bugType match {
      case CLIENT ⇒ clientDir
      case GAME ⇒ gameDir
      case BOT ⇒ botDir
      case ACCOUNT ⇒ accountDir
    }
}

object Bugs {

  case class Bug(bugType: BugType, log: String)

}

class Bugs(config: BugsConfig) extends Actor {
  val bugLog = LoggerFactory.getLogger(getClass)

  var counts = Map(
    CLIENT → 0,
    GAME → 0,
    BOT → 0,
    ACCOUNT → 0
  )

  def receive = {
    case Bug(bugType, log) ⇒
      counts = counts.updated(bugType, counts(bugType) + 1)

      try {
        val out = new PrintWriter(config.dir(bugType) + counts(bugType), "UTF-8")
        try {
          out.print(log)
        } catch {
          case e: Throwable ⇒ bugLog.error("print to file error", e)
        } finally {
          out.close()
        }
      } catch {
        case e: Throwable ⇒ bugLog.error("printWriter error", e)
      }
  }
}

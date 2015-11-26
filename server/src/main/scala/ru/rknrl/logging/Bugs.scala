//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.logging

import java.io.{File, PrintWriter}
import java.util.regex.Pattern

import akka.actor.{Props, Actor}
import ru.rknrl.logging.Bugs._

object Bugs {

  def props(dir: String) = Props(classOf[Bugs], dir)

  case class Bug(log: String)

  val messageMaxSize = 32

  val pattern = Pattern.compile("\\[ERROR\\](.*?)\n")
  val dropSize = "[ERROR]".length

  def getMessage(log: String) = {
    val matcher = pattern.matcher(log)
    val message = if (matcher.find) matcher.group(0).drop(dropSize).dropWhile(_ == ' ').dropRight(1) else "None"
    if (message.length > 0) message.take(messageMaxSize) else "None"
  }
}

class Bugs(dir: String) extends Actor with ActorLog {
  var counts = Map.empty[String, Int]

  def receive = {
    case Bug(logText) ⇒
      val message = getMessage(logText)

      if (counts contains message)
        counts = counts.updated(message, counts(message) + 1)
      else
        counts = counts + (message → 1)

      try {
        val folder = dir + "/" + message + "/"
        new File(folder).mkdirs()
        val out = new PrintWriter(folder + counts(message), "UTF-8")
        try {
          out.print(logText)
        } catch {
          case e: Throwable ⇒ log.error("Bugs", e)
        } finally {
          out.close()
        }
      } catch {
        case e: Throwable ⇒ log.error("Bugs", e)
      }
  }
}

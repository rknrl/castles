//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl

import akka.actor.Props
import org.scalatest.tags.Slow
import ru.rknrl.BugType.BugType
import ru.rknrl.Bugs.Bug
import ru.rknrl.castles.kit.ActorsTest

import scala.concurrent.duration._
import scala.io.Source

@Slow
class BugsTest extends ActorsTest {
  val root = "/Users/tolyayanot/dev/rknrl3/castles/server/src/test/resources/bugs/"
  val config = new BugsConfig(
    clientDir = root + "client/",
    gameDir = root + "game/",
    botDir = root + "bot/",
    accountDir = root + "account/"
  )
  val bugs = system.actorOf(Props(classOf[Bugs], config), "bugs")

  "bug" in {
    BugType.values.foreach(testBug)
  }

  def testBug(bugType: BugType) = {
    val log = System.currentTimeMillis().toString
    bugs ! Bug(bugType, log)
    within(500 millis) {
      expectNoMsg()
    }
    Source.fromFile(config.dir(bugType) + "1").mkString shouldBe log


    val log2 = System.currentTimeMillis().toString
    bugs ! Bug(bugType, log2)
    within(500 millis) {
      expectNoMsg()
    }
    Source.fromFile(config.dir(bugType) + "2").mkString shouldBe log2
  }
}

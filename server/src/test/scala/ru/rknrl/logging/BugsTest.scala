//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.logging

import akka.actor.Props
import org.scalatest.tags.Slow
import ru.rknrl.logging.Bugs
import Bugs.{getMessage, Bug}
import ru.rknrl.test.ActorsTest

import scala.concurrent.duration._
import scala.io.Source

@Slow
class BugsTest extends ActorsTest {
  val dir = "/Users/tolyayanot/dev/rknrl/core/common/common-server/src/test/resources/bugs/"
  val bugs = system.actorOf(Props(classOf[Bugs], dir), "bugs")

  "getMessage" in {
    getMessage("") shouldBe "None"
    getMessage("one two three") shouldBe "None"
    getMessage("[ERrROR] asdsff\n") shouldBe "None"
    getMessage("[ERROR] one two three\n") shouldBe "one two three"
    getMessage("[ERROR]\n") shouldBe "None"
    getMessage("[ERROR]split me split me split me split me split me split me split me split me\n") shouldBe "split me split me split me split"
  }

  "bug" in {
    testBug("Null pointer")
    testBug("Assertion error")
  }

  def testBug(message: String) = {
    val log = "[ERROR] " + message + "\n" + System.currentTimeMillis().toString
    bugs ! Bug(log)
    within(500 millis) {
      expectNoMsg()
    }
    Source.fromFile(dir + "/" + message + "/1").mkString shouldBe log

    val log2 = "[ERROR] " + message + "\n" + System.currentTimeMillis().toString
    bugs ! Bug(log2)
    within(500 millis) {
      expectNoMsg()
    }
    Source.fromFile(dir + "/" + message + "/2").mkString shouldBe log2
  }
}

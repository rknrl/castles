//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.test

import akka.actor.ActorSystem
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class ActorsTest
  extends TestKit(ActorSystem("test-actor-system", TestConfig.config))
  with ImplicitSender with WordSpecLike with Matchers with DefaultTimeout with BeforeAndAfterAll {

  val noMsgTimeout = 100 millis

  def multi(s: String, f: ⇒ Unit) = {
    for (i ← 0 until 1) (s + i) in f
  }

  override protected def afterAll() = system.shutdown()
}

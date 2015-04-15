//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import org.scalatest.{Matchers, WordSpec}

class PeriodObjectTest extends WordSpec with Matchers {

  "non positive duration" in {
    a[Exception] shouldBe thrownBy {
      Period(startTime = 1, duration = 0)
    }
    a[Exception] shouldBe thrownBy {
      Period(startTime = 1, duration = -1)
    }
  }

  "millisFromStart" in {
    val p = Period(startTime = 1, duration = 10)
    p.millisFromStart(1) shouldBe 0
    p.millisFromStart(3) shouldBe 2
  }

  "millisTillEnd" in {
    val p = Period(startTime = 1, duration = 10)
    p.millisTillEnd(1) shouldBe 10
    p.millisTillEnd(3) shouldBe 8
  }

  "isFinish" in {
    val p = Period(startTime = 1, duration = 10)
    p.isFinish(10) shouldBe false
    p.isFinish(11) shouldBe true
    p.isFinish(12) shouldBe true
  }

  "progress" in {
    val p = Period(startTime = 1, duration = 10)
    p.progress(1) shouldBe 0
    p.progress(6) shouldBe 0.5
    p.progress(11) shouldBe 1
  }
}

case class Period(startTime: Long, duration: Long) extends Periodic

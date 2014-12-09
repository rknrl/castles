package ru.rknrl.castles.game.utils

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.utils.PeriodObject

class PeriodObjectTest extends FlatSpec with Matchers {

  class TestPeriodObject(val startTime: Long, val duration: Long) extends PeriodObject[Nothing] {
    def dto(time: Long) = throw new IllegalStateException("unsupported")
  }

  def t = new TestPeriodObject(startTime = 1000, duration = 2000)

  "millisFromStart" should "throw AssertionError when time < startTime" in {
    a[AssertionError] should be thrownBy {
      t.millisFromsStart(500)
    }
  }

  "millisFromStart" should "return correct value" in {
    t.millisFromsStart(1000) should be(0)
    t.millisFromsStart(1555) should be(555)
  }

  "millisTillEnd" should "throw AssertionError when time < startTime" in {
    a[AssertionError] should be thrownBy {
      t.millisTillEnd(500)
    }
  }

  "timeAssert" should "throw AssertionError when time not in period" in {
    a[AssertionError] should be thrownBy {
      t.timeAssert(500)
    }
    a[AssertionError] should be thrownBy {
      t.timeAssert(3001)
    }
  }

  "millisTillEnd" should "return correct value" in {
    t.millisTillEnd(1000) should be(2000)
    t.millisTillEnd(1600) should be(1400)
    t.millisTillEnd(3000) should be(0)
  }
}

package ru.rknrl.castles.game.utils

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.utils.IdIterator

class IdIteratorTest extends FlatSpec with Matchers {

  class TestIdIterator extends IdIterator {
    override def nextInt: Int = super.nextInt
  }

  it should "start from 1 and increase id" in {
    val iterator = new TestIdIterator()
    iterator.nextInt should be(1)
    iterator.nextInt should be(2)
    iterator.nextInt should be(3)
    iterator.nextInt should be(4)
  }

}

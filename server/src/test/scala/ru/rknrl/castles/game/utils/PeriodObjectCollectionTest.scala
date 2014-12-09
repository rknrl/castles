package ru.rknrl.castles.game.utils

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.utils.{PeriodObject, PeriodObjectCollection}

class PeriodObjectCollectionTest extends FlatSpec with Matchers {

  class TestPeriodObjectDTO(val id: Int)

  class TestPeriodObject(val id: Int, val startTime: Long, val duration: Long) extends PeriodObject[TestPeriodObjectDTO] {
    def dto(time: Long) = new TestPeriodObjectDTO(id)
  }

  type TestPeriodObjectCollection = PeriodObjectCollection[TestPeriodObjectDTO, TestPeriodObject]

  val t1 = new TestPeriodObject(id = 1, startTime = 800, duration = 3000)
  val t2 = new TestPeriodObject(id = 2, startTime = 1000, duration = 2000)
  val t3 = new TestPeriodObject(id = 3, startTime = 900, duration = 3000)
  val all = List(t1, t2, t3)

  "add" should "change list" in {
    val b = new TestPeriodObjectCollection(List(t1)).add(List(t2, t3))
    val list = b.list.toList
    list.size should be(3)
    list(0) should be(t1)
    list(1) should be(t2)
    list(2) should be(t3)
  }

  "cleanup" should "work with empty list" in {
    val b = new TestPeriodObjectCollection(List.empty).cleanup(8888)
    b.list.size should be(0)
  }

  "cleanup" should "remove TestPeriodObjectCollection" in {
    val b = new TestPeriodObjectCollection(all).cleanup(3500)
    val list = b.list.toList
    list.size should be(2)
    list(0) should be(t1)
    list(1) should be(t3)
  }

  "cleanup" should "not remove TestPeriodObjectCollection" in {
    val b = new TestPeriodObjectCollection(all).cleanup(1001)
    val list = b.list.toList
    list.size should be(3)
    list(0) should be(t1)
    list(1) should be(t2)
    list(2) should be(t3)
  }

  "dto" should "be correct" in {
    val dto = new TestPeriodObjectCollection(all).dto(1200).toList
    dto.size should be(3)
    dto(0).id should be(1)
    dto(1).id should be(2)
    dto(2).id should be(3)
  }

  "dto" should "work with empty list" in {
    val dto = new TestPeriodObjectCollection(List.empty).dto(1200)
    dto.size should be(0)
  }
}

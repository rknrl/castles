package ru.rknrl.utils

trait PeriodObject[DTO] {
  val startTime: Long
  val duration: Long

  def millisFromsStart(time: Long) = {
    assert(time >= startTime)
    (time - startTime).toInt
  }

  def millisTillEnd(time: Long) = {
    assert(time >= startTime)
    (duration - (time - startTime)).toInt
  }

  def timeAssert(time: Long) =
    assert(time >= startTime && time <= startTime + duration)

  def dto(time: Long): DTO
}

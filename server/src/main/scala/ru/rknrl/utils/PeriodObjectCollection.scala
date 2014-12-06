package ru.rknrl.utils

class PeriodObjectCollection[DTO, T <: PeriodObject[DTO]](val list: Iterable[T]) {
  def add(newList: Iterable[T]) =
    new PeriodObjectCollection[DTO, T](list ++ newList)

  private def isFinish(b: T, time: Long) = time - b.startTime >= b.duration

  def getFinished(time: Long) =
    list.filter(isFinish(_, time))

  def cleanup(time: Long) =
    new PeriodObjectCollection[DTO, T](list.filterNot(isFinish(_, time)))

  def dto(time: Long) = list.map(_.dto(time))
}
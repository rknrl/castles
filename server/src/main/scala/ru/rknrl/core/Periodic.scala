//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.core

import ru.rknrl.Assertion

trait Periodic {
  val startTime: Long
  val duration: Long

  Assertion.check(duration > 0, duration)

  def millisFromStart(time: Long) =
    (time - startTime).toInt

  def millisTillEnd(time: Long) =
    (duration - millisFromStart(time)).toInt

  def isFinish(time: Long) =
    millisFromStart(time) >= duration

  def progress(time: Long) =
    millisFromStart(time).toDouble / duration
}

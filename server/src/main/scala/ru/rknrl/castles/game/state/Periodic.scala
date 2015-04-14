//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import ru.rknrl.Assertion

trait Periodic {
  val startTime: Long
  val duration: Long

  Assertion.check(duration > 0, duration)

  def isFinish(time: Long) =
    time - startTime >= duration

  def millisFromsStart(time: Long) =
    (time - startTime).toInt

  def millisTillEnd(time: Long) =
    (duration - (time - startTime)).toInt

  def progress(time: Long) =
    millisFromsStart(time).toDouble / duration
}

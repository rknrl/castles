//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.core

import ru.rknrl.Assertion

case class Stat(attack: Double,
                defence: Double,
                speed: Double) {

  Assertion.check(attack > 0, attack)
  Assertion.check(defence > 0, defence)
  Assertion.check(speed > 0, speed)

  def *(that: Stat) =
    new Stat(
      attack * that.attack,
      defence * that.defence,
      speed * that.speed
    )
}

object Stat {
  val unit = new Stat(1, 1, 1)
}
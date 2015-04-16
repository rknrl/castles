//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.core

import ru.rknrl.castles.game.{DamagerConfig, GameConfig}
import ru.rknrl.core.Stat
import ru.rknrl.core.points.Point

trait Damager {
  def pos(time: Long): Point
  def damagerConfig: DamagerConfig
}

trait Damaged[A] {
  def count: Double
  def floorCount = GameConfig.truncatePopulation(count)
  def setCount(value: Double): A
  def pos(time: Long): Point
  def stat: Stat
}

object Damage {
  def inRadius[T](damagers: Iterable[Damager], damaged: Damaged[T], time: Long) =
    damagers.filter(d ⇒ d.pos(time).distance(damaged.pos(time)) < d.damagerConfig.radius)

  def applyDamage[A <: Damaged[A]](damaged: A, powers: Iterable[Double]): A = {
    val defence = damaged.stat.defence
    var d = damaged
    for (power ← powers) {
      val damage = power / defence
      val newCount = Math.max(0, d.count - damage)
      d = d.setCount(newCount)
    }
    d
  }
}

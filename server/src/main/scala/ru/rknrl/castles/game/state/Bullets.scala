//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import ru.rknrl.castles.game.GameConfig
import ru.rknrl.dto.BulletDTO

case class Bullet(building: Building,
                  unit: GameUnit,
                  startTime: Long,
                  duration: Long,
                  powerVsUnit: Double) extends Periodic {

  def dto(time: Long) = BulletDTO(building.id, unit.id, duration.toInt)

}

object Bullets {
  def createBullets(buildings: Iterable[Building], units: Iterable[GameUnit], time: Long, config: GameConfig) =
    heads(
      buildings.map(
        b ⇒ units
          .filter(canCreateBullet(b, _, time, config))
          .map(createBullet(b, _, time, config))
      )
    )

  def heads[T](xs: Iterable[Iterable[T]]): Iterable[T] =
    xs.filter(_.size > 0).map(_.head)

  def canCreateBullet(b: Building, u: GameUnit, time: Long, config: GameConfig) = {
    val distance = b.pos.distance(u.pos(time))
    val duration = (distance / config.shooting.speed).toLong
    distance < config.shooting.shootRadius && !u.isFinish(time + duration)
  }

  def createBullet(b: Building, u: GameUnit, time: Long, config: GameConfig) = {
    val duration = b.pos.distance(u.pos(time)) / config.shooting.speed

    new Bullet(
      b,
      u,
      startTime = time,
      duration = duration.toLong,
      powerVsUnit = config.bulletPowerVsUnit(b)
    )
  }
}

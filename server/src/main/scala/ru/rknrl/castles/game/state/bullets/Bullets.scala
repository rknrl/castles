//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state.bullets

import ru.rknrl.castles.game.GameConfig
import ru.rknrl.castles.game.points.Point
import ru.rknrl.castles.game.state.buildings.{Building, Buildings}
import ru.rknrl.castles.game.state.units.{GameUnit, GameUnits}
import ru.rknrl.castles.rmi.B2C.AddBullet
import ru.rknrl.dto.GameDTO.BulletDTO
import ru.rknrl.utils.PeriodObjectCollection

object Bullets {
  type Bullets = PeriodObjectCollection[BulletDTO, Bullet]

  def createBullets(buildings: Buildings, units: GameUnits, time: Long, config: GameConfig) =
    createAvailableBullets(buildings, units, time, config)
      .filter(_.nonEmpty)
      .map(_.head)

  private def createAvailableBullets(buildings: Buildings, units: GameUnits, time: Long, config: GameConfig) =
    for (b ← buildings.canShoot(time, config);
         nearestUnits = getNearestUnits(b.pos, units, time, config, config.shootRadius))
      yield
      nearestUnitsToBullets(b, nearestUnits, time, config)

  private def getNearestUnits(buildingPos: Point, units: GameUnits, time: Long, config: GameConfig, shootRadius: Double) =
    for (u ← units.units;
         unitPos = u.pos(time)
         if buildingPos.distance(unitPos) < shootRadius)
      yield u

  private def nearestUnitsToBullets(b: Building, nearestUnits: Iterable[GameUnit], time: Long, config: GameConfig) =
    nearestUnits
      .filter(u ⇒ Some(u.owner) != b.owner)
      .map(createBullet(b, _, time, config))
      .filter(bullet ⇒ bullet.unit.pos(time + bullet.duration) != bullet.unit.endPos)

  private def createBullet(b: Building, unit: GameUnit, time: Long, config: GameConfig) = {
    val unitPos = unit.pos(time)
    val duration = b.pos.duration(unitPos, config.bulletSpeed)
    new Bullet(b, unit, startTime = time, duration = duration.toLong)
  }

  def `bullets→addMessage`(bullets: Iterable[Bullet], time: Long) =
    bullets.map(b ⇒ AddBullet(b.dto(time)))
}

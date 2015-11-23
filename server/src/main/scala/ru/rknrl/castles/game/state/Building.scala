//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import protos._
import ru.rknrl.Assertion
import ru.rknrl.castles.game.GameConfig
import ru.rknrl.core.points.Point
import ru.rknrl.core.{Damage, Damaged, Damager, Stat}

class Building(val id: BuildingId,
               val buildingPrototype: BuildingPrototype,
               val count: Double,
               val pos: Point,
               val buildingStat: Stat,
               val owner: Option[Player],
               val strengthening: Option[Strengthening],
               val lastShootTime: Long) extends Damaged[Building] {

  Assertion.check(count >= 0, count)

  def pos(time: Long) = pos

  def stat = buildingStat *
    (if (owner.isDefined) owner.get.stat else Stat.unit) *
    (if (strengthening.isDefined) strengthening.get.stat else Stat.unit)

  def regenerate(deltaTime: Long, config: GameConfig) = {
    val maxCount = config.buildings(buildingPrototype).maxCount
    val newCount = count + deltaTime * config.buildings(buildingPrototype).regeneration
    copy(newCount = Math.min(maxCount, newCount))
  }

  def applyStrengthening(strengthenings: Iterable[Strengthening]) = {
    val myStrengthenings = strengthenings.filter(_.buildingId == id)
    if (myStrengthenings.nonEmpty)
      copy(newStrengthening = Some(myStrengthenings.head))
    else
      this
  }

  def cleanupStrengthening(time: Long) =
    if (strengthening.isDefined && strengthening.get.isFinish(time))
      copy(newStrengthening = None)
    else
      this

  def applyExitUnits(exitUnits: Iterable[GameUnit]) = {
    val myExitUnits = exitUnits.filter(_.fromBuilding.id == id)

    var b = this

    for (exitUnit ← myExitUnits)
      b = copy(newCount = b.count - exitUnit.count)

    b
  }

  def applyEnterUnits(enterUnits: Iterable[GameUnit], config: GameConfig) = {
    val myEnterUnits = enterUnits.filter(_.toBuilding.id == id)

    var b = this

    for (enterUnit ← myEnterUnits)
      if (owner == Some(enterUnit.owner)) {
        val newCount = config.countAfterFriendlyUnitEnter(b, enterUnit.count)
        b = b.copy(newCount = newCount)
      } else {
        val (newCount, capture) = config.countAfterEnemyUnitEnter(b, enterUnit, stat, enterUnit.stat)
        val newOwner = if (capture) Some(enterUnit.owner) else owner
        b = copy(newCount = newCount, newOwner = newOwner)
      }

    b
  }

  def applyDamagers(damagers: Iterable[Damager], time: Long) = {
    val damagersInRadius = Damage.inRadius(damagers, this, time)
    val powers = damagersInRadius.map(_.damagerConfig.powerVsBuilding)
    Damage.applyDamage(this, powers)
  }

  def applyShots(time: Long, bullets: Iterable[Bullet]) =
    if (bullets.exists(_.building.id == id))
      copy(newLastShootTime = time)
    else
      this


  def setCount(newCount: Double) = copy(newCount = newCount)

  def copy(newCount: Double = count,
           newOwner: Option[Player] = owner,
           newStrengthening: Option[Strengthening] = strengthening,
           newLastShootTime: Long = lastShootTime) =
    new Building(
      id = id,
      buildingPrototype = buildingPrototype,
      count = newCount,
      pos = pos,
      buildingStat = buildingStat,
      owner = newOwner,
      strengthening = newStrengthening,
      lastShootTime = newLastShootTime
    )

  def differentWith(b: Building) = floorCount != b.floorCount || owner != b.owner || strengthening.isDefined != b.strengthening.isDefined

  def dto =
    BuildingDTO(
      id,
      buildingPrototype,
      pos.dto,
      floorCount,
      if (owner.isDefined) Some(owner.get.id) else None,
      strengthening.isDefined
    )

  def updateDto =
    BuildingUpdate(
      id,
      floorCount,
      if (owner.isDefined) Some(owner.get.id) else None,
      strengthening.isDefined
    )

}

object Building {
  def canShoot(buildings: Iterable[Building], time: Long, config: GameConfig) =
    buildings.filter(b ⇒
      b.buildingPrototype.buildingType == BuildingType.TOWER &&
        time - b.lastShootTime > config.shooting.shootInterval
    )
}

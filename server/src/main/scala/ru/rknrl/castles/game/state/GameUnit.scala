//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import ru.rknrl.Assertion
import ru.rknrl.castles.rmi.B2C.UpdateUnit
import ru.rknrl.dto.{UnitDTO, UnitId, UnitUpdateDTO}

class GameUnit(val id: UnitId,
               val startTime: Long,
               val duration: Long,
               val fromBuilding: Building,
               val toBuilding: Building,
               val count: Double) extends Damaged[GameUnit] with Movable {

  Assertion.check(count >= 0, count)

  val points = Points(fromBuilding.pos, toBuilding.pos)

  def buildingPrototype = fromBuilding.buildingPrototype

  def owner = fromBuilding.owner.get

  def stat = fromBuilding.stat

  def strengthened = fromBuilding.strengthening.isDefined

  def setCount(newCount: Double) = copy(newCount = newCount)

  def applyDamagers(damagers: Iterable[Damager], time: Long) = {
    val damagersInRadius = Damage.inRadius(damagers, this, time)
    val powers = damagersInRadius.map(_.damagerConfig.powerVsUnit)
    Damage.applyDamage(this, powers)
  }

  def applyBullets(bullets: Iterable[Bullet]) = {
    val myBullets = bullets.filter(_.unit.id == id)
    val powers = myBullets.map(_.powerVsUnit)
    Damage.applyDamage(this, powers)
  }

  def copy(newCount: Double = count) =
    new GameUnit(
      id = id,
      startTime = startTime,
      duration = duration,
      fromBuilding = fromBuilding,
      toBuilding = toBuilding,
      count = newCount
    )

  def differentWith(that: GameUnit) = this.floorCount != that.floorCount

  def dto(time: Long) =
    UnitDTO(
      id = id,
      buildingType = buildingPrototype.buildingType,
      count = floorCount,
      pos = pos(time).dto,
      duration = duration.toInt,
      targetBuildingId = toBuilding.id,
      owner = owner.id,
      strengthened = strengthened
    )

  def updateDto =
    UnitUpdateDTO(
      id = id,
      count = floorCount
    )

}

object GameUnit {
  def getUpdateMessages(oldUnits: Iterable[GameUnit], newUnits: Iterable[GameUnit]) =
    for (newUnit ← newUnits;
         oldUnit = oldUnits.find(_.id == newUnit.id)
         if oldUnit.isDefined
         if oldUnit.get differentWith newUnit
    ) yield UpdateUnit(newUnit.updateDto)
}
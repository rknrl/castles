//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state.buildings

import ru.rknrl.Assertion
import ru.rknrl.castles.game.GameConfig
import ru.rknrl.castles.game.points.Point
import ru.rknrl.dto.CommonDTO.BuildingPrototypeDTO
import ru.rknrl.dto.GameDTO.{BuildingDTO, BuildingIdDTO, BuildingUpdateDTO, PlayerIdDTO}

object BuildingId {
  def apply(id: Int) = BuildingIdDTO.newBuilder.setId(id).build
}

class Building(val id: BuildingIdDTO,
               val prototype: BuildingPrototypeDTO,
               val pos: Point,
               val population: Double,
               val owner: Option[PlayerIdDTO],
               val strengthened: Boolean,
               val strengtheningStartTime: Long,
               val lastShootTime: Long) {

  Assertion.check(population >= 0, population)

  def floorPopulation = GameConfig.truncatePopulation(population)

  def setPopulation(value: Double) = copy(newPopulation = value)

  def setOwner(value: Option[PlayerIdDTO]) = copy(newOwner = value)

  def strength(time: Long) = copy(newStrengthened = true, newStrengtheningStartTime = time)

  def unstrength() = copy(newStrengthened = false)

  def shoot(time: Long) = copy(newLastShootTime = time)

  private def copy(newPopulation: Double = population,
                   newOwner: Option[PlayerIdDTO] = owner,
                   newStrengthened: Boolean = strengthened,
                   newStrengtheningStartTime: Long = strengtheningStartTime,
                   newLastShootTime: Long = lastShootTime) =
    new Building(
      id,
      prototype,
      pos,
      newPopulation,
      newOwner,
      newStrengthened,
      newStrengtheningStartTime,
      newLastShootTime
    )

  def differentWith(b: Building) = floorPopulation != b.floorPopulation || owner != b.owner || strengthened != b.strengthened

  def dto = {
    val builder = BuildingDTO.newBuilder
      .setId(id)
      .setBuilding(prototype)
      .setPos(pos.dto)
      .setPopulation(floorPopulation)
      .setStrengthened(strengthened)

    if (owner.isDefined)
      builder.setOwner(owner.get)

    builder.build
  }

  def updateDto = {
    val builder = BuildingUpdateDTO.newBuilder
      .setId(id)
      .setPopulation(floorPopulation)
      .setStrengthened(strengthened)

    if (owner.isDefined)
      builder.setOwner(owner.get)

    builder.build
  }
}

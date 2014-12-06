package ru.rknrl.castles.game.objects.buildings

import ru.rknrl.castles.account.objects.BuildingPrototype
import ru.rknrl.castles.game.GameConfig
import ru.rknrl.castles.game.objects.players.PlayerId
import ru.rknrl.dto.GameDTO.{BuildingDTO, BuildingIdDTO, BuildingUpdateDTO}
import ru.rknrl.utils.Point

class BuildingId(val id: Int) {
  override def equals(obj: Any) = obj match {
    case buildingId: BuildingId ⇒ buildingId.id == id
    case _ ⇒ false
  }

  override def hashCode = id.hashCode

  def dto = BuildingIdDTO.newBuilder().setId(id).build()
}

class Building(val id: BuildingId,
               val prototype: BuildingPrototype,
               val x: Double,
               val y: Double,
               val population: Double,
               val owner: Option[PlayerId],
               val strengthened: Boolean,
               val strengtheningStartTime: Long,
               val lastShootTime: Long) {

  assert(population >= 0)

  def pos = new Point(x, y)

  def floorPopulation = GameConfig.truncatePopulation(population)

  def setPopulation(value: Double) = copy(newPopulation = value)

  def addPopulation(amount: Double) = setPopulation(population + amount)

  def setOwner(value: Option[PlayerId]) = copy(newOwner = value)

  def strength(time: Long) = copy(newStrengthened = true, newStrengtheningStartTime = time)

  def unstrength() = copy(newStrengthened = false)

  def shoot(time: Long) = copy(newLastShootTime = time)

  private def copy(newPopulation: Double = population,
                   newOwner: Option[PlayerId] = owner,
                   newStrengthened: Boolean = strengthened,
                   newStrengtheningStartTime: Long = strengtheningStartTime,
                   newLastShootTime: Long = lastShootTime) =
    new Building(
      id,
      prototype,
      x,
      y,
      newPopulation,
      newOwner,
      newStrengthened,
      newStrengtheningStartTime,
      newLastShootTime
    )

  def differentWith(b: Building) = floorPopulation != b.floorPopulation || owner != b.owner || strengthened != b.strengthened

  def dto = {
    val builder = BuildingDTO.newBuilder()
      .setId(id.dto)
      .setBuilding(prototype.dto)
      .setX(x.toFloat)
      .setY(y.toFloat)
      .setPopulation(floorPopulation)
      .setStrengthened(strengthened)

    if (owner.isDefined)
      builder.setOwner(owner.get.dto)

    builder.build()
  }

  def updateDto = {
    val builder = BuildingUpdateDTO.newBuilder()
      .setId(id.dto)
      .setPopulation(floorPopulation)
      .setStrengthened(strengthened)

    if (owner.isDefined)
      builder.setOwner(owner.get.dto)

    builder.build()
  }
}

package ru.rknrl.castles.game.objects.units

import ru.rknrl.castles.account.objects.BuildingPrototype
import ru.rknrl.castles.game.objects.players.PlayerId
import ru.rknrl.castles.game.{GameConfig, Stat}
import ru.rknrl.castles.game.objects.buildings.BuildingId
import ru.rknrl.utils.Point
import ru.rknrl.dto.GameDTO.{UnitDTO, UnitIdDTO, UnitUpdateDTO}

class UnitId(val id: Int) {
  override def equals(obj: Any) = obj match {
    case unitId: UnitId ⇒ unitId.id == id
    case _ ⇒ false
  }

  override def hashCode = id.hashCode

  def dto = UnitIdDTO.newBuilder().setId(id).build()
}

class GameUnit(val id: UnitId,
               val buildingPrototype: BuildingPrototype,
               val count: Double,
               val startPos: Point,
               val endPos: Point,
               val startTime: Long,
               val speed: Double,
               val targetBuildingId: BuildingId,
               val owner: PlayerId,
               val strengthened: Boolean) {

  def setCount(value: Double) = copy(newCount = value)

  def getPos(time: Long) = startPos.lerp(endPos, startTime, time, speed)

  private def copy(newCount: Double = count,
                   newSpeed: Double = speed) =
    new GameUnit(
      id,
      buildingPrototype,
      newCount,
      startPos,
      endPos,
      startTime,
      newSpeed,
      targetBuildingId,
      owner,
      strengthened)

  def differentWith(u: GameUnit) = count != u.count || speed != u.speed

  def dto(time: Long) = {
    val pos = getPos(time)

    UnitDTO.newBuilder()
      .setId(id.dto)
      .setType(buildingPrototype.buildingType)
      .setCount(GameConfig.truncatePopulation(count))
      .setX(pos.x.toFloat)
      .setY(pos.y.toFloat)
      .setSpeed(speed.toFloat)
      .setTargetBuildingId(targetBuildingId.dto)
      .setOwner(owner.dto)
      .setStrengthened(strengthened)
      .build()
  }

  def updateDto(time: Long) = {
    val pos = getPos(time)

    UnitUpdateDTO.newBuilder()
      .setId(id.dto)
      .setX(pos.x.toFloat)
      .setY(pos.y.toFloat)
      .setSpeed(speed.toFloat)
      .setCount(GameConfig.truncatePopulation(count))
      .build()
  }
}
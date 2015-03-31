//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state.units

import ru.rknrl.castles.game.GameConfig
import ru.rknrl.castles.game.points.Point
import ru.rknrl.dto.CommonDTO.BuildingPrototypeDTO
import ru.rknrl.dto.GameDTO._

object UnitId {
  def apply(id: Int) = UnitIdDTO.newBuilder.setId(id).build
}

class GameUnit(val id: UnitIdDTO,
               val buildingPrototype: BuildingPrototypeDTO,
               val count: Double,
               val startPos: Point,
               val endPos: Point,
               val startTime: Long,
               val speed: Double,
               val targetBuildingId: BuildingIdDTO,
               val owner: PlayerIdDTO,
               val strengthened: Boolean) {

  // todo count может быть 0, в ситуации после получения юнитами дамага

  def floorCount = GameConfig.truncatePopulation(count)

  def setCount(value: Double) = copy(newCount = value)

  def pos(time: Long) = startPos.lerp(endPos, startTime, time, speed)

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
      strengthened
    )

  def differentWith(u: GameUnit) = count != u.count || speed != u.speed

  def dto(time: Long) =
    UnitDTO.newBuilder
      .setId(id)
      .setType(buildingPrototype.getType)
      .setCount(GameConfig.truncatePopulation(count))
      .setPos(pos(time).dto)
      .setSpeed(speed.toFloat)
      .setTargetBuildingId(targetBuildingId)
      .setOwner(owner)
      .setStrengthened(strengthened)
      .build

  def updateDto(time: Long) =
    UnitUpdateDTO.newBuilder
      .setId(id)
      .setPos(pos(time).dto)
      .setSpeed(speed.toFloat)
      .setCount(GameConfig.truncatePopulation(count))
      .build
}
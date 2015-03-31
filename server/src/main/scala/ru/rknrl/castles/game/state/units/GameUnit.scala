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
import ru.rknrl.castles.game.state.buildings.BuildingId
import ru.rknrl.castles.game.state.players.PlayerId
import ru.rknrl.dto.CommonDTO.BuildingPrototypeDTO
import ru.rknrl.dto.GameDTO.{PlayerIdDTO, UnitDTO, UnitIdDTO, UnitUpdateDTO}

case class UnitId(id: Int) {
  def dto = UnitIdDTO.newBuilder.setId(id).build
}

class GameUnit(val id: UnitId,
               val buildingPrototype: BuildingPrototypeDTO,
               val count: Double,
               val startPos: Point,
               val endPos: Point,
               val startTime: Long,
               val speed: Double,
               val targetBuildingId: BuildingId,
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
      .setId(id.dto)
      .setType(buildingPrototype.getType)
      .setCount(GameConfig.truncatePopulation(count))
      .setPos(pos(time).dto)
      .setSpeed(speed.toFloat)
      .setTargetBuildingId(targetBuildingId.dto)
      .setOwner(owner)
      .setStrengthened(strengthened)
      .build

  def updateDto(time: Long) =
    UnitUpdateDTO.newBuilder
      .setId(id.dto)
      .setPos(pos(time).dto)
      .setSpeed(speed.toFloat)
      .setCount(GameConfig.truncatePopulation(count))
      .build
}
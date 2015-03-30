//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account.state

import ru.rknrl.castles.account.state.BuildingPrototype._
import ru.rknrl.dto.CommonDTO.{BuildingLevel, BuildingPrototypeDTO, BuildingType}

class BuildingPrototype private(val buildingType: BuildingType,
                                val level: BuildingLevel) {

  def upgraded = BuildingPrototype(buildingType, nextLevel(level))

  def dto = BuildingPrototypeDTO.newBuilder()
    .setType(buildingType)
    .setLevel(level)
    .build

  override def equals(obj: Any) = obj match {
    case b: BuildingPrototype ⇒ b.buildingType == buildingType && b.level == level
    case _ ⇒ false
  }

  override def hashCode = (buildingType.toString + "_" + level.toString).hashCode
}

object BuildingPrototype {
  private def nextLevel(level: BuildingLevel) =
    level match {
      case BuildingLevel.LEVEL_1 ⇒ BuildingLevel.LEVEL_2
      case BuildingLevel.LEVEL_2 ⇒ BuildingLevel.LEVEL_3
    }

  def apply(buildingType: BuildingType, buildingLevel: BuildingLevel): BuildingPrototype =
    new BuildingPrototype(buildingType, buildingLevel)

  def apply(dto: BuildingPrototypeDTO): BuildingPrototype =
    apply(dto.getType, dto.getLevel)
}

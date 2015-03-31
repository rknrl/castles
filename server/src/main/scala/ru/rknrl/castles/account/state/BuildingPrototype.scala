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

case class BuildingPrototype(buildingType: BuildingType,
                             level: BuildingLevel) {

  def upgraded = BuildingPrototype(buildingType, nextLevel(level))

  def dto = BuildingPrototypeDTO.newBuilder
    .setType(buildingType)
    .setLevel(level)
    .build
}

object BuildingPrototype {
  private def nextLevel(level: BuildingLevel) =
    level match {
      case BuildingLevel.LEVEL_1 ⇒ BuildingLevel.LEVEL_2
      case BuildingLevel.LEVEL_2 ⇒ BuildingLevel.LEVEL_3
    }

  def apply(dto: BuildingPrototypeDTO): BuildingPrototype =
    apply(dto.getType, dto.getLevel)
}

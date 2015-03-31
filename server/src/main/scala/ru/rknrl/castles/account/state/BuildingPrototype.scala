//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account.state

import ru.rknrl.dto.CommonDTO.{BuildingLevel, BuildingPrototypeDTO, BuildingType}

object BuildingPrototype {

  def apply(buildingType: BuildingType, buildingLevel: BuildingLevel) =
    BuildingPrototypeDTO.newBuilder
      .setType(buildingType)
      .setLevel(buildingLevel)
      .build

  def upgraded(prototypeDTO: BuildingPrototypeDTO) =
    BuildingPrototypeDTO.newBuilder
      .setType(prototypeDTO.getType)
      .setLevel(nextLevel(prototypeDTO.getLevel))
      .build

  private def nextLevel(level: BuildingLevel) =
    level match {
      case BuildingLevel.LEVEL_1 ⇒ BuildingLevel.LEVEL_2
      case BuildingLevel.LEVEL_2 ⇒ BuildingLevel.LEVEL_3
    }
}

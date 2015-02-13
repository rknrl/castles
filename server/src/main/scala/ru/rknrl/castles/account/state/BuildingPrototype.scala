//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account.state

import ru.rknrl.dto.CommonDTO.{BuildingLevel, BuildingPrototypeDTO, BuildingType}

class BuildingPrototype(val buildingType: BuildingType,
                        val level: BuildingLevel) {
  def dto = BuildingPrototypeDTO.newBuilder()
    .setType(buildingType)
    .setLevel(level)
    .build()

  override def equals(obj: Any) = obj match {
    case b: BuildingPrototype ⇒ b.buildingType.getNumber == buildingType.getNumber && b.level.getNumber == level.getNumber
    case _ ⇒ false
  }

  override def hashCode = (buildingType.toString + "_" + level.toString).hashCode
}

object BuildingPrototype {
  def getNextLevel(level: BuildingLevel) =
    level match {
      case BuildingLevel.LEVEL_1 ⇒ BuildingLevel.LEVEL_2
      case BuildingLevel.LEVEL_2 ⇒ BuildingLevel.LEVEL_3
      case _ ⇒ throw new IllegalStateException("hasn't next level " + level)
    }

  def all =
    for (buildingType ← BuildingType.values();
         level ← BuildingLevel.values())
    yield new BuildingPrototype(buildingType, level)

  def fromDto(dto: BuildingPrototypeDTO) = new BuildingPrototype(dto.getType, dto.getLevel)
}

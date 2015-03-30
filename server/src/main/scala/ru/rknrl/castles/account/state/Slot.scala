//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account.state

import ru.rknrl.dto.AccountDTO.SlotDTO
import ru.rknrl.dto.CommonDTO.{BuildingLevel, BuildingType, SlotId}

class Slot(val id: SlotId,
           val buildingPrototype: Option[BuildingPrototype]) {

  def set(buildingPrototype: BuildingPrototype) =
    new Slot(id, Some(buildingPrototype))

  def remove = new Slot(id, None)

  def build(buildingType: BuildingType) = {
    assert(buildingPrototype.isEmpty)
    new Slot(id, Some(BuildingPrototype(buildingType, BuildingLevel.LEVEL_1)))
  }

  def upgrade = {
    assert(buildingPrototype.isDefined)
    new Slot(id, Some(buildingPrototype.get.upgraded))
  }

  def dto = {
    val builder = SlotDTO.newBuilder().setId(id)
    if (buildingPrototype.isDefined) builder.setBuildingPrototype(buildingPrototype.get.dto)
    builder.build
  }
}

object Slot {
  def fromDto(dto: SlotDTO) =
    new Slot(
      dto.getId,
      if (dto.hasBuildingPrototype) Some(BuildingPrototype(dto.getBuildingPrototype)) else None
    )
}

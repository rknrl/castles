//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account.state

import ru.rknrl.dto.AccountDTO.SlotDTO
import ru.rknrl.dto.CommonDTO.SlotId

class Slot(val id: SlotId,
           val buildingPrototype: Option[BuildingPrototype]) {
  def dto = {
    val builder = SlotDTO.newBuilder.setId(id)
    if (buildingPrototype.isDefined) builder.setBuildingPrototype(buildingPrototype.get.dto)
    builder.build
  }
}

object Slot {
  def apply(id: SlotId, prototype: BuildingPrototype) =
    new Slot(id, Some(prototype))

  def empty(id: SlotId) =
    new Slot(id, None)

  def apply(dto: SlotDTO) =
    new Slot(
      dto.getId,
      if (dto.hasBuildingPrototype) Some(BuildingPrototype(dto.getBuildingPrototype)) else None
    )
}

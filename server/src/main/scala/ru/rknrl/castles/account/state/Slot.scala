//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account.state

import ru.rknrl.dto.AccountDTO.SlotDTO
import ru.rknrl.dto.CommonDTO.{BuildingPrototypeDTO, SlotId}

object Slot {
  def apply(id: SlotId, prototype: BuildingPrototypeDTO): SlotDTO =
    apply(id, Some(prototype))

  def empty(id: SlotId): SlotDTO =
    apply(id, None)

  def apply(id: SlotId, prototype: Option[BuildingPrototypeDTO]): SlotDTO = {
    val builder = SlotDTO.newBuilder.setId(id)
    if (prototype.isDefined) builder.setBuildingPrototype(prototype.get)
    builder.build
  }
}

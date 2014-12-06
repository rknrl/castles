package ru.rknrl.castles.account.objects

import ru.rknrl.dto.AccountDTO.SlotDTO
import ru.rknrl.dto.CommonDTO.{BuildingLevel, BuildingType, SlotId}

class StartLocationSlot(val id: SlotId,
                        val buildingPrototype: Option[BuildingPrototype]) {

  def remove = {
    assert(buildingPrototype.isDefined)
    new StartLocationSlot(id, None)
  }

  def build(buildingType: BuildingType) = {
    assert(buildingPrototype.isEmpty)
    new StartLocationSlot(id, Some(new BuildingPrototype(buildingType, BuildingLevel.LEVEL_1)))
  }

  def upgrade = {
    assert(buildingPrototype.isDefined)
    val nextLevel = BuildingPrototype.getNextLevel(buildingPrototype.get.level)
    new StartLocationSlot(id, Some(new BuildingPrototype(buildingPrototype.get.buildingType, nextLevel)))
  }

  def dto = {
    val builder = SlotDTO.newBuilder().setId(id)
    if (buildingPrototype.isDefined) builder.setBuildingPrototype(buildingPrototype.get.dto)
    builder.build()
  }
}

package ru.rknrl.castles.rmi.c2b

import ru.rknrl.dto.AdminDTO._

abstract class AdminFacade {
  def getAccountState(dto: AdminGetAccountStateDTO)

  def addGold(dto: AdminAddGoldDTO)

  def addItem(dto: AdminAddItemDTO)

  def setSkill(dto: AdminSetSkillDTO)

  def setSlot(dto: AdminSetSlotDTO)
}

package ru.rknrl.castles.rmi.b2c

import ru.rknrl.dto.AdminDTO.AdminAccountStateDTO

abstract class AdminFacade {
  def accountState(dto: AdminAccountStateDTO)
}

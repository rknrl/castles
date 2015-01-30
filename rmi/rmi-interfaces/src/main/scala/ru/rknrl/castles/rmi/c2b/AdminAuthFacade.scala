package ru.rknrl.castles.rmi.c2b

import ru.rknrl.dto.AdminDTO._

abstract class AdminAuthFacade {
  def authenticateAsAdmin(authenticate: AdminAuthenticateDTO)
}

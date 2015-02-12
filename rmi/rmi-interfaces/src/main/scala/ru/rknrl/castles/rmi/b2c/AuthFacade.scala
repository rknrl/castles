package ru.rknrl.castles.rmi.b2c

import ru.rknrl.dto.AuthDTO.AuthenticatedDTO

abstract class AuthFacade {
  def authReady()

  def authenticated(success: AuthenticatedDTO)
}

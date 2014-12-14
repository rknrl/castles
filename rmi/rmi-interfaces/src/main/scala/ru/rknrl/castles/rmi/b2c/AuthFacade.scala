package ru.rknrl.castles.rmi.b2c

import ru.rknrl.dto.AuthDTO.AuthenticationSuccessDTO

abstract class AuthFacade {
  def authReady()

  def authenticationSuccess(success: AuthenticationSuccessDTO)
}

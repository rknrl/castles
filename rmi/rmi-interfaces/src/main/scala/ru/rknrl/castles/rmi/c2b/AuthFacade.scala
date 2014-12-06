package ru.rknrl.castles.rmi.c2b

import ru.rknrl.dto.AuthDTO.AuthenticateDTO

abstract class AuthFacade {
  def authenticate(authenticate: AuthenticateDTO)
}

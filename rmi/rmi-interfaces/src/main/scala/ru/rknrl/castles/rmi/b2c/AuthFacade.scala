package ru.rknrl.castles.rmi.b2c

import ru.rknrl.dto.AccountDTO.AccountStateDTO

abstract class AuthFacade {
  def authenticationResult(state: AccountStateDTO)
}

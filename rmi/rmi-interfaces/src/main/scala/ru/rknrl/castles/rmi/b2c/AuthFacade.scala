package ru.rknrl.castles.rmi.b2c

import ru.rknrl.dto.AccountDTO.AccountStateDTO

abstract class AuthFacade {
  def authReady()

  def authenticationResult(state: AccountStateDTO)
}

package ru.rknrl.castles.database

import ru.rknrl.base.AccountId
import ru.rknrl.dto.AccountDTO.AccountStateDTO

object AccountStateDb {

  case class Insert(accountId: AccountId, accountState: AccountStateDTO)

  case class Update(accountId: AccountId, accountState: AccountStateDTO)

  case class Get(accountId: AccountId)

  case object NoExist

  case object GetTop

}
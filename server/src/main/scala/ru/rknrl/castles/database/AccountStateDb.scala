package ru.rknrl.castles.database

import ru.rknrl.base.AccountId
import ru.rknrl.dto.AccountDTO.AccountStateDTO

object AccountStateDb {

  case class Insert(key: AccountId, value: AccountStateDTO)

  case class Update(key: AccountId, value: AccountStateDTO)

  case class Get(key: AccountId)

  case object NoExist

  case object GetTop
}
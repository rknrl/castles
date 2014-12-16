package ru.rknrl.castles.database

import ru.rknrl.castles.AccountId
import ru.rknrl.dto.AccountDTO.AccountStateDTO

object AccountStateDb {

  case class Put(key: AccountId, value: AccountStateDTO)

  case class Get(key: AccountId)

  case class NoExist(key: AccountId)

}
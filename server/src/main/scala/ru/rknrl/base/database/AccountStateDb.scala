package ru.rknrl.base.database

import ru.rknrl.base.AccountId
import ru.rknrl.dto.AccountDTO.AccountStateDTO
import ru.rknrl.dto.CommonDTO.UserInfoDTO

object AccountStateDb {

  case class Insert(accountId: AccountId, accountState: AccountStateDTO, userInfo: UserInfoDTO)

  case class Update(accountId: AccountId, accountState: AccountStateDTO)

  case class UpdateUserInfo(accountId: AccountId, userInfo: UserInfoDTO)

  case class Get(accountId: AccountId)

  case object NoExist

  case object GetTop

}
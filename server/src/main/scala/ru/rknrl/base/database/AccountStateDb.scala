package ru.rknrl.base.database

import ru.rknrl.dto.AccountDTO.AccountStateDTO
import ru.rknrl.dto.CommonDTO.{AccountIdDTO, UserInfoDTO}

object AccountStateDb {
  /** Ответом будет StateResponse */
  case class Insert(accountId: AccountIdDTO, accountState: AccountStateDTO, userInfo: UserInfoDTO)

  /** Ответом будет StateResponse */
  case class Update(accountId: AccountIdDTO, accountState: AccountStateDTO)

  /** Ответом будет StateResponse или NoExist */
  case class Get(accountId: AccountIdDTO)

  case class StateResponse(accountId: AccountIdDTO, state: AccountStateDTO)

  case object NoExist

  /** Ответом будет UserInfoDTO */
  case class UpdateUserInfo(accountId: AccountIdDTO, userInfo: UserInfoDTO)

  /** Ответом будет List[TopItem] */
  case object GetTop

}
//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.database

import ru.rknrl.dto.AccountDTO.AccountStateDTO
import ru.rknrl.dto.CommonDTO.{AccountIdDTO, UserInfoDTO}

object Database {

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
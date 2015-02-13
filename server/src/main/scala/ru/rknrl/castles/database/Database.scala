//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.database

import ru.rknrl.dto.AccountDTO.AccountStateDTO
import ru.rknrl.dto.CommonDTO.{AccountIdDTO, TutorStateDTO, UserInfoDTO}

object Database {

  /** Ответом будет StateResponse */
  case class Insert(accountId: AccountIdDTO, accountState: AccountStateDTO, userInfo: UserInfoDTO, tutorStateDTO: TutorStateDTO)

  /** Ответом будет StateResponse */
  case class Update(accountId: AccountIdDTO, accountState: AccountStateDTO)

  /** Ответом будет AccountStateResponse или NoExist */
  case class GetAccountState(accountId: AccountIdDTO)

  case class AccountStateResponse(accountId: AccountIdDTO, state: AccountStateDTO)

  case object AccountNoExists

  /** Ответом будет TutorStateResponse или TutorNoExist */
  case class GetTutorState(accountId: AccountIdDTO)

  case class TutorStateResponse(accountId: AccountIdDTO, tutorState: TutorStateDTO)

  case class UpdateTutorState(accountId: AccountIdDTO, tutorState: TutorStateDTO)

  /** Ответом будет UserInfoDTO */
  case class UpdateUserInfo(accountId: AccountIdDTO, userInfo: UserInfoDTO)

  /** Ответом будет List[TopItem] */
  case object GetTop
  
}
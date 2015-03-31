//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles

import ru.rknrl.dto.CommonDTO.{AccountIdDTO, AccountType}

case class AccountId(accountType: AccountType,
                     id: String) {
  def dto = AccountIdDTO.newBuilder
    .setType(accountType)
    .setId(id)
    .build
}

object AccountId {
  def apply(dto: AccountIdDTO): AccountId =
    apply(dto.getType, dto.getId)
}
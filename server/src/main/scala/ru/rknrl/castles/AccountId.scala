//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles

import ru.rknrl.dto.CommonDTO.{AccountIdDTO, AccountType}

class AccountId private(val accountType: AccountType,
                        val id: String) {

  override def equals(obj: scala.Any) = obj match {
    case accountId: AccountId ⇒ accountId.accountType == accountType && accountId.id == id
    case _ ⇒ false
  }

  override def hashCode = toString.hashCode

  override def toString = accountType.toString + " " + id

  def dto = AccountIdDTO.newBuilder()
    .setId(id)
    .setType(accountType)
    .build()
}

object AccountId {
  def apply(accountType: AccountType, id: String): AccountId =
    new AccountId(accountType, id)

  def apply(dto: AccountIdDTO): AccountId =
    apply(dto.getType, dto.getId)
}
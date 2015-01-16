package ru.rknrl.castles

import ru.rknrl.dto.CommonDTO.{AccountIdDTO, AccountType}

class AccountId(val accountType: AccountType,
                val id: String) {

  def this(dto: AccountIdDTO) = {
    this(dto.getType, dto.getId)
  }

  override def equals(obj: scala.Any) = obj match {
    case accountId: AccountId ⇒ accountId.accountType == accountType && accountId.id == id
    case _ ⇒ false
  }

  override def hashCode = (accountType.getNumber + id).hashCode

  def dto = AccountIdDTO.newBuilder()
    .setId(id)
    .setType(accountType)
    .build()
}
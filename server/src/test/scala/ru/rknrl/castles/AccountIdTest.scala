package ru.rknrl.castles

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.base.AccountId
import ru.rknrl.dto.CommonDTO.{AccountIdDTO, AccountType}

class AccountIdTest extends FlatSpec with Matchers {
  "AccountId" should "correct initialize by dto" in {
    val accountType = AccountType.VKONTAKTE
    val dto = AccountIdDTO.newBuilder()
      .setType(accountType)
      .setId("externalId")
      .build()

    val id = new AccountId(dto)
    id.accountType should be(accountType)
    id.id should be("externalId")
  }

  "AccountId.equals" should "be false with other types" in {
    val id = new AccountId(AccountType.VKONTAKTE, "127")
    (id == "id") should be(false)
  }

  "AccountId.equals" should "be true with same id" in {
    val id = new AccountId(AccountType.VKONTAKTE, "127")
    val id2 = new AccountId(AccountType.VKONTAKTE, "127")
    (id == id2) should be(true)
  }

  "AccountId.equals" should "be false with different id" in {
    (new AccountId(AccountType.VKONTAKTE, "127") == new AccountId(AccountType.VKONTAKTE, "128")) should be(false)
    (new AccountId(AccountType.VKONTAKTE, "127") == new AccountId(AccountType.ODNOKLASSNIKI, "127")) should be(false)
  }

  "PlayerId" should "have correct hash" in {
    Map[AccountId, String]()
      .updated(new AccountId(AccountType.VKONTAKTE, "127"), "a")
      .updated(new AccountId(AccountType.VKONTAKTE, "127"), "b")
      .apply(new AccountId(AccountType.VKONTAKTE, "127")) should be("b")

    Map[AccountId, String]()
      .updated(new AccountId(AccountType.MOIMIR, "127"), "a")
      .updated(new AccountId(AccountType.VKONTAKTE, "127"), "b")
      .apply(new AccountId(AccountType.MOIMIR, "127")) should be("a")
  }
}

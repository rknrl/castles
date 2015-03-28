//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles

import org.scalatest.{FunSuite, Matchers}
import ru.rknrl.dto.CommonDTO.{AccountIdDTO, AccountType}

class AccountIdTest extends FunSuite with Matchers {
  test("accountId == такому же accountId") {
    val id = new AccountId(AccountType.VKONTAKTE, "264648879")
    id shouldEqual id
    new AccountId(AccountType.VKONTAKTE, "264648879") shouldEqual new AccountId(AccountType.VKONTAKTE, "264648879")
    new AccountId(AccountType.VKONTAKTE, "9221545") shouldEqual new AccountId(AccountType.VKONTAKTE, "9221545")
    new AccountId(AccountType.DEV, "bot0") shouldEqual new AccountId(AccountType.DEV, "bot0")
  }

  test("accountId != отличному accountId") {
    new AccountId(AccountType.VKONTAKTE, "264648879") shouldNot equal(new AccountId(AccountType.VKONTAKTE, "9221545"))
    new AccountId(AccountType.VKONTAKTE, "9221545") shouldNot equal(new AccountId(AccountType.VKONTAKTE, "264648879"))
    new AccountId(AccountType.VKONTAKTE, "264648879") shouldNot equal(new AccountId(AccountType.DEV, "bot0"))
  }

  test("accountId should NOT be equals with diff types") {
    new AccountId(AccountType.VKONTAKTE, "264648879") shouldNot equal("264648879")
    new AccountId(AccountType.VKONTAKTE, "9221545") shouldNot equal("9221545")
    new AccountId(AccountType.DEV, "bot0") shouldNot equal("bot0")
  }

  test("разные accountId корректно работают в мапе") {
    var set = Map.empty[AccountId, Boolean]
    set = set + (new AccountId(AccountType.VKONTAKTE, "264648879") → true) + (new AccountId(AccountType.VKONTAKTE, "9221545") → true)

    set.size should be(2)
    set.contains(new AccountId(AccountType.VKONTAKTE, "264648879")) should be(true)
    set.contains(new AccountId(AccountType.VKONTAKTE, "9221545")) should be(true)
    set.contains(new AccountId(AccountType.DEV, "bot0")) should be(false)
  }

  test("одинаковые accountId корректно работают в мапе") {
    var set = Map.empty[AccountId, Boolean]
    set = set + (new AccountId(AccountType.VKONTAKTE, "264648879") → true) + (new AccountId(AccountType.VKONTAKTE, "264648879") → true)

    set.size should be(1)
    set.contains(new AccountId(AccountType.VKONTAKTE, "264648879")) should be(true)
    set.contains(new AccountId(AccountType.VKONTAKTE, "9221545")) should be(false)
    set.contains(new AccountId(AccountType.DEV, "bot0")) should be(false)
  }

  test("accountId выдает корректный dto") {
    new AccountId(AccountType.VKONTAKTE, "264648879").dto.getType should be(AccountType.VKONTAKTE)
    new AccountId(AccountType.VKONTAKTE, "264648879").dto.getId should be("264648879")

    new AccountId(AccountType.DEV, "bot0").dto.getType should be(AccountType.DEV)
    new AccountId(AccountType.DEV, "bot0").dto.getId should be("bot0")
  }

  test("accountId выдает корректный toString") {
    new AccountId(AccountType.VKONTAKTE, "264648879").toString should be("VKONTAKTE 264648879")
    new AccountId(AccountType.DEV, "bot0").toString should be("DEV bot0")
  }

  test("accountId корректно создается из dto") {
    val dto1 = AccountIdDTO.newBuilder()
      .setType(AccountType.VKONTAKTE)
      .setId("264648879")
      .build
    new AccountId(dto1).accountType should be(AccountType.VKONTAKTE)
    new AccountId(dto1).id should be("264648879")

    val dto2 = AccountIdDTO.newBuilder()
      .setType(AccountType.DEV)
      .setId("bot0")
      .build
    new AccountId(dto2).accountType should be(AccountType.DEV)
    new AccountId(dto2).id should be("bot0")
  }
}

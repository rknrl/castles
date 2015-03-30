//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles

import org.scalatest.{FreeSpec, Matchers}
import ru.rknrl.TestUtils._
import ru.rknrl.dto.CommonDTO.AccountIdDTO
import ru.rknrl.dto.CommonDTO.AccountType._

class AccountIdTest extends FreeSpec with Matchers {
  val all = Seq(
    () ⇒ new AccountId(VKONTAKTE, "264648879"),
    () ⇒ new AccountId(VKONTAKTE, "9221545"),
    () ⇒ new AccountId(DEV, "bot0")
  )

  "equals" in {
    checkEquals(all)
  }

  "hashCode" in {
    checkEquals(all)
  }

  "dto" in {
    new AccountId(VKONTAKTE, "264648879").dto.getType should be(VKONTAKTE)
    new AccountId(VKONTAKTE, "264648879").dto.getId should be("264648879")

    new AccountId(DEV, "bot0").dto.getType should be(DEV)
    new AccountId(DEV, "bot0").dto.getId should be("bot0")
  }

  "toString" in {
    new AccountId(VKONTAKTE, "264648879").toString should be("VKONTAKTE 264648879")
    new AccountId(DEV, "bot0").toString should be("DEV bot0")
  }

  "parse from dto" in {
    val dto1 = AccountIdDTO.newBuilder()
      .setType(VKONTAKTE)
      .setId("264648879")
      .build
    new AccountId(dto1).accountType should be(VKONTAKTE)
    new AccountId(dto1).id should be("264648879")

    val dto2 = AccountIdDTO.newBuilder()
      .setType(DEV)
      .setId("bot0")
      .build
    new AccountId(dto2).accountType should be(DEV)
    new AccountId(dto2).id should be("bot0")
  }
}

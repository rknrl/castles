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
    () ⇒ AccountId(VKONTAKTE, "264648879"),
    () ⇒ AccountId(VKONTAKTE, "9221545"),
    () ⇒ AccountId(DEV, "bot0")
  )

  "equals" in {
    checkEquals(all)
  }

  "hashCode" in {
    checkHashCode(all)
  }

  "dto" in {
    AccountId(VKONTAKTE, "264648879").dto.getType should be(VKONTAKTE)
    AccountId(VKONTAKTE, "264648879").dto.getId should be("264648879")

    AccountId(DEV, "bot0").dto.getType should be(DEV)
    AccountId(DEV, "bot0").dto.getId should be("bot0")
  }

  "toString" in {
    AccountId(VKONTAKTE, "264648879").toString should be("VKONTAKTE 264648879")
    AccountId(DEV, "bot0").toString should be("DEV bot0")
  }

  "parse from dto" in {
    val dto1 = AccountIdDTO.newBuilder()
      .setType(VKONTAKTE)
      .setId("264648879")
      .build
    AccountId(dto1).accountType should be(VKONTAKTE)
    AccountId(dto1).id should be("264648879")

    val dto2 = AccountIdDTO.newBuilder()
      .setType(DEV)
      .setId("bot0")
      .build
    AccountId(dto2).accountType should be(DEV)
    AccountId(dto2).id should be("bot0")
  }
}

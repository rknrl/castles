//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.matchmaking

import org.scalatest.{Matchers, WordSpec}
import protos.AccountId
import protos.AccountType.VKONTAKTE
import ru.rknrl.castles.kit.Mocks._

class ELOTest extends WordSpec with Matchers {
  "getNewRating" in {
    // Новичок выигрывает противника с рейтингом 1600 в бою два на два
    val newRating = ELO.getRatingAmount(ratingA = 1400, ratingB = 1600, gamesCountA = 1, sA = 1)
    newRating shouldBe (22.0 +- 1)
  }

  "getNewRating2" in {
    // Новичок выигрывает такого же новичка в бою два на два
    val newRating = ELO.getRatingAmount(ratingA = 1400, ratingB = 1400, gamesCountA = 1, sA = 1)
    newRating shouldBe (15.0 +- 1)
  }

  "newRating" in {
    // like getNewRating2 test
    val order1 = newGameOrder(
      AccountId(VKONTAKTE, "1"),
      accountState = accountStateMock(gamesCount = 1),
      rating = 1400
    )
    val order2 = newGameOrder(
      AccountId(VKONTAKTE, "2"),
      accountState = accountStateMock(gamesCount = 1),
      rating = 1400
    )
    val newRating = ELO.ratingAmount(List(order1, order2), order1, place = 1)
    newRating shouldBe (15.0 +- 1)
  }

}

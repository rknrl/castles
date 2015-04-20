//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.matchmaking

import org.scalatest.{Matchers, WordSpec}

class ELOTest extends WordSpec with Matchers {
  "getNewRating" in {
    // Новичок выигрывает противника с рейтингом 1600 в бою два на два
    val newRating = ELO.getNewRating(ratingA = 1400, ratingB = 1600, gamesCountA = 1, sA = 1)
    newRating shouldBe (1422.0 +- 1)
  }

  "getNewRating2" in {
    // Новичок выигрывает такого же новичка в бою два на два
    val newRating = ELO.getNewRating(ratingA = 1400, ratingB = 1400, gamesCountA = 1, sA = 1)
    newRating shouldBe (1415.0 +- 1)
  }

}

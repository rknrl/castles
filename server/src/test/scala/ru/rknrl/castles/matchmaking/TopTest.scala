//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.matchmaking

import org.scalatest.{Matchers, WordSpec}
import protos.AccountType.{FACEBOOK, ODNOKLASSNIKI, VKONTAKTE}
import protos.{AccountId, TopUserInfo, UserInfo}

class TopTest extends WordSpec with Matchers {
  val id1 = AccountId(VKONTAKTE, "1")
  val id2 = AccountId(FACEBOOK, "1")
  val id3 = AccountId(ODNOKLASSNIKI, "2")
  val id4 = AccountId(ODNOKLASSNIKI, "4")
  val id5 = AccountId(ODNOKLASSNIKI, "5")

  val info1 = UserInfo(id1)
  val info2 = UserInfo(id2)
  val info3 = UserInfo(id3)
  val info4 = UserInfo(id4)
  val info5 = UserInfo(id5)

  val top5 = new Top(
    List(
      TopUser(id1, 1512.2, info1),
      TopUser(id2, 1400, info2),
      TopUser(id3, 1399, info3),
      TopUser(id4, 1300, info4),
      TopUser(id5, 1200, info5)
    ),
    weekNumber = 1
  )

  val top3 = new Top(
    List(
      TopUser(id1, 1512.2, info1),
      TopUser(id2, 1400, info2),
      TopUser(id3, 1399, info3)
    ),
    weekNumber = 2
  )

  "dto" in {
    top5.dto.weekNumber shouldBe 1
    top5.dto.users.size shouldBe 5
    top5.dto.users(0) shouldBe TopUserInfo(place = 1, info1)
    top5.dto.users(1) shouldBe TopUserInfo(place = 2, info2)
    top5.dto.users(2) shouldBe TopUserInfo(place = 3, info3)
    top5.dto.users(3) shouldBe TopUserInfo(place = 4, info4)
    top5.dto.users(4) shouldBe TopUserInfo(place = 5, info5)

    top3.dto.weekNumber shouldBe 2
    top3.dto.users.size shouldBe 3
    top3.dto.users(0) shouldBe TopUserInfo(place = 1, info1)
    top3.dto.users(1) shouldBe TopUserInfo(place = 2, info2)
    top3.dto.users(2) shouldBe TopUserInfo(place = 3, info3)
  }

  "insert" should {
    "last" in {
      val id6 = AccountId(ODNOKLASSNIKI, "6")
      val info6 = UserInfo(id6)

      top5.insert(TopUser(id6, 1000, info6)).users shouldBe top5.users
    }

    "first" in {
      val id6 = AccountId(ODNOKLASSNIKI, "6")
      val info6 = UserInfo(id6)

      top5.insert(TopUser(id6, 8000, info6)).users shouldBe
        List(
          TopUser(id6, 8000, info6),
          TopUser(id1, 1512.2, info1),
          TopUser(id2, 1400, info2),
          TopUser(id3, 1399, info3),
          TopUser(id4, 1300, info4)
        )
    }

    "middle" in {
      val id6 = AccountId(ODNOKLASSNIKI, "6")
      val info6 = UserInfo(id6)

      top5.insert(TopUser(id6, 1380, info6)).users shouldBe
        List(
          TopUser(id1, 1512.2, info1),
          TopUser(id2, 1400, info2),
          TopUser(id3, 1399, info3),
          TopUser(id6, 1380, info6),
          TopUser(id4, 1300, info4)
        )
    }

    "already exists" in {
      top5.insert(TopUser(id2, 8000, info2)).users shouldBe
        List(
          TopUser(id2, 8000, info2),
          TopUser(id1, 1512.2, info1),
          TopUser(id3, 1399, info3),
          TopUser(id4, 1300, info4),
          TopUser(id5, 1200, info5)
        )
    }

    "3" in {
      val id6 = AccountId(ODNOKLASSNIKI, "6")
      val info6 = UserInfo(id6)

      top3.insert(TopUser(id6, 1000, info6)).users shouldBe
        List(
          TopUser(id1, 1512.2, info1),
          TopUser(id2, 1400, info2),
          TopUser(id3, 1399, info3),
          TopUser(id6, 1000, info6)
        )
    }
  }


}

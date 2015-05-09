//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.matchmaking

import org.scalatest.{Matchers, WordSpec}
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.castles.matchmaking.Matcher.MatchedGameOrders
import ru.rknrl.dto.AccountId
import ru.rknrl.dto.AccountType.VKONTAKTE
import ru.rknrl.dto.DeviceType.{PC, PHONE, TABLET}

class MatcherTest extends WordSpec with Matchers {

  val o0 = newGameOrder(
    AccountId(VKONTAKTE, "0"),
    accountState = accountStateMock(
      gamesCount = 8
    ),
    rating = 1400,
    deviceType = TABLET
  )
  val o1 = newGameOrder(
    AccountId(VKONTAKTE, "1"),
    accountState = accountStateMock(
      gamesCount = 0 // <- isTutor
    ),
    rating = 1400,
    deviceType = TABLET
  )
  val o2 = newGameOrder(
    AccountId(VKONTAKTE, "2"),
    accountState = accountStateMock(
      gamesCount = 1
    ),
    rating = 1450,
    deviceType = PC
  )
  val o3 = newGameOrder(
    AccountId(VKONTAKTE, "3"),
    accountState = accountStateMock(
      gamesCount = 2
    ),
    rating = 1800,
    deviceType = PHONE
  )

  val orders = List(o0, o1, o2, o3)

  "groupOrders2" in {
    Matcher.groupOrders(playersCount = 2, orders) shouldBe List(
      MatchedGameOrders(playersCount = 2, orders = List(o1), isTutor = true),
      MatchedGameOrders(playersCount = 2, orders = List(o3, o2), isTutor = false),
      MatchedGameOrders(playersCount = 2, orders = List(o0), isTutor = false)
    )
  }

  "groupOrders4" in {
    Matcher.groupOrders(playersCount = 4, orders) shouldBe List(
      MatchedGameOrders(playersCount = 4, orders = List(o1), isTutor = true),
      MatchedGameOrders(playersCount = 4, orders = List(o3, o2, o0), isTutor = false)
    )
  }

  "matchOrders2" in {
    Matcher.matchOrders(orders) shouldBe List(
      MatchedGameOrders(playersCount = 2, orders = List(o3), isTutor = false),
      MatchedGameOrders(playersCount = 4, orders = List(o1), isTutor = true),
      MatchedGameOrders(playersCount = 4, orders = List(o2, o0), isTutor = false)
    )
  }
}

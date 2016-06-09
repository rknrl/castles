//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.storage

import akka.testkit.TestProbe
import org.scalatest.Matchers
import protos.AccountId
import protos.AccountType.VKONTAKTE
import protos.StatAction.{TUTOR_2_LOSE, _}
import ru.rknrl.castles.kit.Mocks
import ru.rknrl.castles.storage.Statistics.{createGameStatistics, leaveGameStatistics}
import ru.rknrl.test.ActorsTest

class StatisticsTest extends ActorsTest with Matchers {
  val human = Mocks.newGameOrder(AccountId(VKONTAKTE, "1"), isBot = false)
  val bot = Mocks.newGameOrder(AccountId(VKONTAKTE, "2"), isBot = true)

  "sendLeaveGameStatistics tutor" in {
    implicit val client = new TestProbe(system)

    leaveGameStatistics(
      place = 1,
      isTutor = true,
      orders = List(human, bot),
      order = human
    ) shouldBe Some(TUTOR_2_WIN)

    leaveGameStatistics(
      place = 2,
      isTutor = true,
      orders = List(human, bot),
      order = human
    ) shouldBe Some(TUTOR_2_LOSE)

    leaveGameStatistics(
      place = 1,
      isTutor = true,
      orders = List(human, bot, bot, bot),
      order = human
    ) shouldBe Some(TUTOR_4_WIN)

    leaveGameStatistics(
      place = 2,
      isTutor = true,
      orders = List(human, bot, bot, bot),
      order = human
    ) shouldBe Some(TUTOR_4_LOSE)
  }

  "sendLeaveGameStatistics not tutor" in {
    implicit val client = new TestProbe(system)

    leaveGameStatistics(
      place = 1,
      isTutor = false,
      orders = List(human, bot),
      order = human
    ) shouldBe Some(WIN_2_BOTS)

    leaveGameStatistics(
      place = 2,
      isTutor = false,
      orders = List(human, bot),
      order = human
    ) shouldBe Some(LOSE_2_BOTS)

    leaveGameStatistics(
      place = 1,
      isTutor = false,
      orders = List(human, bot, bot, bot),
      order = human
    ) shouldBe Some(WIN_4_BOTS)

    leaveGameStatistics(
      place = 2,
      isTutor = false,
      orders = List(human, bot, bot, bot),
      order = human
    ) shouldBe Some(LOSE_4_BOTS)
  }

  "sendCreateGameStatistics" in {
    implicit val client = new TestProbe(system)

    createGameStatistics(List(human, bot)) shouldBe START_GAME_2_WITH_BOTS

    createGameStatistics(List(human, human)) shouldBe START_GAME_2_WITH_PLAYERS

    createGameStatistics(List(human, bot, bot, bot)) shouldBe START_GAME_4_WITH_BOTS

    createGameStatistics(List(human, human, bot, bot)) shouldBe START_GAME_4_WITH_PLAYERS
  }


}

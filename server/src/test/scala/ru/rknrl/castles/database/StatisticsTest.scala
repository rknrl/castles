//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.database

import akka.testkit.TestProbe
import org.scalatest.Matchers
import protos.AccountId
import protos.AccountType.VKONTAKTE
import protos.StatAction._
import ru.rknrl.castles.kit.Mocks
import ru.rknrl.test.ActorsTest

class StatisticsTest extends ActorsTest with Matchers {
  val human = Mocks.newGameOrder(AccountId(VKONTAKTE, "1"), isBot = false)
  val bot = Mocks.newGameOrder(AccountId(VKONTAKTE, "2"), isBot = true)

  "sendLeaveGameStatistics tutor" in {
    implicit val client = new TestProbe(system)
    val graphite = new TestProbe(system)

    Statistics.sendLeaveGameStatistics(
      place = 1,
      isTutor = true,
      orders = List(human, bot),
      order = human,
      graphite.ref
    )
    graphite.expectMsg(TUTOR_2_WIN)

    Statistics.sendLeaveGameStatistics(
      place = 2,
      isTutor = true,
      orders = List(human, bot),
      order = human,
      graphite.ref
    )
    graphite.expectMsg(TUTOR_2_LOSE)

    Statistics.sendLeaveGameStatistics(
      place = 1,
      isTutor = true,
      orders = List(human, bot, bot, bot),
      order = human,
      graphite.ref
    )
    graphite.expectMsg(TUTOR_4_WIN)

    Statistics.sendLeaveGameStatistics(
      place = 2,
      isTutor = true,
      orders = List(human, bot, bot, bot),
      order = human,
      graphite.ref
    )
    graphite.expectMsg(TUTOR_4_LOSE)
  }

  "sendLeaveGameStatistics not tutor" in {
    implicit val client = new TestProbe(system)
    val graphite = new TestProbe(system)

    Statistics.sendLeaveGameStatistics(
      place = 1,
      isTutor = false,
      orders = List(human, bot),
      order = human,
      graphite.ref
    )
    graphite.expectMsg(WIN_2_BOTS)

    Statistics.sendLeaveGameStatistics(
      place = 2,
      isTutor = false,
      orders = List(human, bot),
      order = human,
      graphite.ref
    )
    graphite.expectMsg(LOSE_2_BOTS)

    Statistics.sendLeaveGameStatistics(
      place = 1,
      isTutor = false,
      orders = List(human, bot, bot, bot),
      order = human,
      graphite.ref
    )
    graphite.expectMsg(WIN_4_BOTS)

    Statistics.sendLeaveGameStatistics(
      place = 2,
      isTutor = false,
      orders = List(human, bot, bot, bot),
      order = human,
      graphite.ref
    )
    graphite.expectMsg(LOSE_4_BOTS)
  }

  "sendCreateGameStatistics" in {
    implicit val client = new TestProbe(system)
    val graphite = new TestProbe(system)

    Statistics.sendCreateGameStatistics(List(human, bot), graphite.ref)
    graphite.expectMsg(START_GAME_2_WITH_BOTS)

    Statistics.sendCreateGameStatistics(List(human, human), graphite.ref)
    graphite.expectMsg(START_GAME_2_WITH_PLAYERS)

    Statistics.sendCreateGameStatistics(List(human, bot, bot, bot), graphite.ref)
    graphite.expectMsg(START_GAME_4_WITH_BOTS)

    Statistics.sendCreateGameStatistics(List(human, human, bot, bot), graphite.ref)
    graphite.expectMsg(START_GAME_4_WITH_PLAYERS)
  }


}

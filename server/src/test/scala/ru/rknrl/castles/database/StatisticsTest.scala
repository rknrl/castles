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
import ru.rknrl.castles.kit.{ActorsTest, Mocks}
import ru.rknrl.dto.AccountId
import ru.rknrl.dto.AccountType.VKONTAKTE
import ru.rknrl.dto.StatAction._

class StatisticsTest extends ActorsTest with Matchers {
  val human = Mocks.newGameOrder(AccountId(VKONTAKTE, "1"), isBot = false)
  val bot = Mocks.newGameOrder(AccountId(VKONTAKTE, "2"), isBot = true)

  "sendLeaveGameStatistics tutor" in {
    implicit val client = new TestProbe(system)
    val database = new TestProbe(system)

    Statistics.sendLeaveGameStatistics(
      place = 1,
      isTutor = true,
      orders = List(human, bot),
      order = human,
      database.ref
    )
    database.expectMsg(TUTOR_2_WIN)

    Statistics.sendLeaveGameStatistics(
      place = 2,
      isTutor = true,
      orders = List(human, bot),
      order = human,
      database.ref
    )
    database.expectMsg(TUTOR_2_LOSE)

    Statistics.sendLeaveGameStatistics(
      place = 1,
      isTutor = true,
      orders = List(human, bot, bot, bot),
      order = human,
      database.ref
    )
    database.expectMsg(TUTOR_4_WIN)

    Statistics.sendLeaveGameStatistics(
      place = 2,
      isTutor = true,
      orders = List(human, bot, bot, bot),
      order = human,
      database.ref
    )
    database.expectMsg(TUTOR_4_LOSE)
  }

  "sendLeaveGameStatistics not tutor" in {
    implicit val client = new TestProbe(system)
    val database = new TestProbe(system)

    Statistics.sendLeaveGameStatistics(
      place = 1,
      isTutor = false,
      orders = List(human, bot),
      order = human,
      database.ref
    )
    database.expectMsg(WIN_2_BOTS)

    Statistics.sendLeaveGameStatistics(
      place = 2,
      isTutor = false,
      orders = List(human, bot),
      order = human,
      database.ref
    )
    database.expectMsg(LOSE_2_BOTS)

    Statistics.sendLeaveGameStatistics(
      place = 1,
      isTutor = false,
      orders = List(human, bot, bot, bot),
      order = human,
      database.ref
    )
    database.expectMsg(WIN_4_BOTS)

    Statistics.sendLeaveGameStatistics(
      place = 2,
      isTutor = false,
      orders = List(human, bot, bot, bot),
      order = human,
      database.ref
    )
    database.expectMsg(LOSE_4_BOTS)
  }

  "sendCreateGameStatistics" in {
    implicit val client = new TestProbe(system)
    val database = new TestProbe(system)

    Statistics.sendCreateGameStatistics(List(human, bot), database.ref)
    database.expectMsg(START_GAME_2_WITH_BOTS)

    Statistics.sendCreateGameStatistics(List(human, human), database.ref)
    database.expectMsg(START_GAME_2_WITH_PLAYERS)

    Statistics.sendCreateGameStatistics(List(human, bot, bot, bot), database.ref)
    database.expectMsg(START_GAME_4_WITH_BOTS)

    Statistics.sendCreateGameStatistics(List(human, human, bot, bot), database.ref)
    database.expectMsg(START_GAME_4_WITH_PLAYERS)
  }


}

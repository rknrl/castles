//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account

import akka.testkit.TestProbe
import protos.BuildingLevel.{LEVEL_1, LEVEL_2}
import protos.BuildingType.{HOUSE, TOWER}
import protos.ItemType.FIREBALL
import protos.SkillLevel.SKILL_LEVEL_1
import protos.SkillType.ATTACK
import protos.SlotId.{SLOT_1, SLOT_3}
import protos._
import ru.rknrl.castles.account.AccountState._
import ru.rknrl.castles.database.DatabaseTransaction.GetAccount
import ru.rknrl.castles.database.{DatabaseTransaction, FakeCalendar, Statistics}
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.castles.matchmaking.MatchMaking.GameOrder
import ru.rknrl.castles.matchmaking.Top

import scala.concurrent.duration._

class AccountTest extends AccountTestSpec {
  val TIMEOUT = 100 millis
  val config = configMock()
  val accountState = accountStateMock(gold = 1000)

  multi("AcceptPresent", {
    check(
      expectedAccountState = acceptPresent(Some(accountState), config.account, new FakeCalendar(week = 3)),
      clientMessage = AcceptPresent,
      statMessage = None
    )
  })

  multi("AcceptAdvert", {
    check(
      expectedAccountState = acceptAdvert(Some(accountState), true, config.account),
      clientMessage = AcceptAdvert(true),
      statMessage = None
    )
  })

  multi("AcceptWeekTop", {
    check(
      expectedAccountState = acceptWeekTop(Some(accountState), config.account, 3),
      clientMessage = AcceptWeekTop(WeekNumber(3)),
      statMessage = None
    )
  })

  multi("BuyBuilding", {
    check(
      expectedAccountState = buyBuilding(Some(accountState), SLOT_1, TOWER, config.account),
      clientMessage = BuyBuilding(SLOT_1, TOWER),
      statMessage = Statistics.buyBuilding(TOWER, LEVEL_1)
    )
  })

  multi("UpgradeBuilding", {
    check(
      expectedAccountState = upgradeBuilding(Some(accountState), SLOT_3, config.account),
      clientMessage = UpgradeBuilding(SLOT_3),
      statMessage = Statistics.buyBuilding(HOUSE, LEVEL_2)
    )
  })

  multi("RemoveBuilding", {
    check(
      expectedAccountState = removeBuilding(Some(accountState), SLOT_3, config.account),
      clientMessage = RemoveBuilding(SLOT_3),
      statMessage = StatAction.REMOVE_BUILDING
    )
  })

  multi("UpgradeSkill", {
    check(
      expectedAccountState = upgradeSkill(Some(accountState), ATTACK, config.account),
      clientMessage = UpgradeSkill(ATTACK),
      statMessage = Statistics.buySkill(ATTACK, SKILL_LEVEL_1)
    )
  })

  multi("BuyItem", {
    check(
      expectedAccountState = buyItem(Some(accountState), FIREBALL, config.account),
      clientMessage = BuyItem(FIREBALL),
      statMessage = Statistics.buyItem(FIREBALL)
    )
  })

  def check(expectedAccountState: AccountState,
            clientMessage: Any,
            statMessage: StatAction): Unit =
    check(expectedAccountState, clientMessage, Some(statMessage))

  def check(expectedAccountState: AccountState,
            clientMessage: Any,
            statMessage: Option[StatAction]): Unit = {
    val secretChecker = new TestProbe(system)
    val database = new TestProbe(system)
    val graphite = new TestProbe(system)
    val client = new TestProbe(system)
    val matchmaking = new TestProbe(system)
    val account = newAccount(
      secretChecker = secretChecker.ref,
      database = database.ref,
      graphite = graphite.ref,
      matchmaking = matchmaking.ref,
      config = config
    )
    val accountId = authenticateMock().userInfo.accountId
    authorize(
      secretChecker = secretChecker,
      matchmaking = matchmaking,
      database = database,
      graphite = graphite,
      client = client,
      account = account,
      config = config,
      accountState = accountState
    )

    client.send(account, clientMessage)

    database.expectMsgPF(TIMEOUT) {
      case DatabaseTransaction.GetAndUpdateAccountState(accountId, transform) ⇒
        val newState = transform(Some(accountState))
        newState shouldBe expectedAccountState
    }
    if (statMessage.isDefined) graphite.expectMsg(statMessage.get)
    database.send(account, DatabaseTransaction.AccountStateResponse(accountId, expectedAccountState))

    client.expectMsg(expectedAccountState)
  }

  multi("EnterGame", {
    val secretChecker = new TestProbe(system)
    val database = new TestProbe(system)
    val graphite = new TestProbe(system)
    val client = new TestProbe(system)
    val matchmaking = new TestProbe(system)
    val account = newAccount(
      secretChecker = secretChecker.ref,
      database = database.ref,
      graphite = graphite.ref,
      matchmaking = matchmaking.ref,
      config = config
    )
    val accountId = authenticateMock().userInfo.accountId
    authorize(
      secretChecker = secretChecker,
      matchmaking = matchmaking,
      database = database,
      graphite = graphite,
      client = client,
      account = account,
      config = config,
      accountState = accountState
    )

    client.send(account, EnterGame)

    database.expectMsg(GetAccount(accountId))
    database.send(account, DatabaseTransaction.AccountResponse(
      accountId,
      state = Some(accountState),
      rating = Some(config.account.initRating),
      tutorState = None,
      place = Some(999),
      top = new Top(List.empty, 5),
      lastWeekPlace = Some(666),
      lastWeekTop = new Top(List.empty, 5)
    ))

    matchmaking.expectMsg(
      GameOrder(
        accountId,
        authenticateMock().deviceType,
        authenticateMock().userInfo,
        accountState,
        rating = config.account.initRating,
        isBot = false
      )
    )
  })
}

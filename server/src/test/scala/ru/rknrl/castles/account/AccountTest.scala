//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account

import akka.testkit.TestProbe
import ru.rknrl.castles.account.AccountState._
import ru.rknrl.castles.database.DatabaseTransaction.GetAccount
import ru.rknrl.castles.database.{DatabaseTransaction, Statistics}
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.castles.matchmaking.MatchMaking.GameOrder
import ru.rknrl.castles.matchmaking.Top
import ru.rknrl.castles.rmi.B2C.AccountStateUpdated
import ru.rknrl.castles.rmi.C2B._
import ru.rknrl.dto.BuildingLevel.{LEVEL_1, LEVEL_2}
import ru.rknrl.dto.BuildingType.{HOUSE, TOWER}
import ru.rknrl.dto.ItemType.FIREBALL
import ru.rknrl.dto.SkillLevel.SKILL_LEVEL_1
import ru.rknrl.dto.SkillType.ATTACK
import ru.rknrl.dto.SlotId.{SLOT_1, SLOT_3}
import ru.rknrl.dto._

import scala.concurrent.duration._

class AccountTest extends AccountTestSpec {
  val TIMEOUT = 100 millis
  val config = configMock()
  val accountState = accountStateMock(gold = 1000)

  multi("BuyBuilding", {
    check(
      expectedAccountState = buyBuilding(accountState, SLOT_1, TOWER, config.account),
      clientMessage = BuyBuilding(BuyBuildingDTO(SLOT_1, TOWER)),
      statMessage = Statistics.buyBuilding(TOWER, LEVEL_1)
    )
  })

  multi("UpgradeBuilding", {
    check(
      expectedAccountState = upgradeBuilding(accountState, SLOT_3, config.account),
      clientMessage = UpgradeBuilding(UpgradeBuildingDTO(SLOT_3)),
      statMessage = Statistics.buyBuilding(HOUSE, LEVEL_2)
    )
  })

  multi("RemoveBuilding", {
    check(
      expectedAccountState = removeBuilding(accountState, SLOT_3),
      clientMessage = RemoveBuilding(RemoveBuildingDTO(SLOT_3)),
      statMessage = StatAction.REMOVE_BUILDING
    )
  })

  multi("UpgradeSkill", {
    check(
      expectedAccountState = upgradeSkill(accountState, ATTACK, config.account),
      clientMessage = UpgradeSkill(UpgradeSkillDTO(ATTACK)),
      statMessage = Statistics.buySkill(ATTACK, SKILL_LEVEL_1)
    )
  })

  multi("BuyItem", {
    check(
      expectedAccountState = buyItem(accountState, FIREBALL, config.account),
      clientMessage = BuyItem(BuyItemDTO(FIREBALL)),
      statMessage = Statistics.buyItem(FIREBALL)
    )
  })

  def check(expectedAccountState: AccountStateDTO,
            clientMessage: Any,
            statMessage: Any) = {
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
    graphite.expectMsg(statMessage)
    database.send(account, DatabaseTransaction.AccountStateResponse(accountId, expectedAccountState))

    client.expectMsg(AccountStateUpdated(expectedAccountState))
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
      place = 999,
      top = new Top(List.empty, 5),
      lastWeekPlace = 666,
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

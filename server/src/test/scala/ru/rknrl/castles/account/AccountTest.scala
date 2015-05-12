//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account

import akka.testkit.TestProbe
import ru.rknrl.castles.database.Database.{AccountStateResponse, UpdateAccountState}
import ru.rknrl.castles.database.Statistics
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.castles.matchmaking.MatchMaking.GameOrder
import ru.rknrl.castles.rmi.B2C.AccountStateUpdated
import ru.rknrl.castles.rmi.C2B._
import ru.rknrl.dto.BuildingLevel.{LEVEL_1, LEVEL_2}
import ru.rknrl.dto.BuildingType.{HOUSE, TOWER}
import ru.rknrl.dto.ItemType.FIREBALL
import ru.rknrl.dto.SkillLevel.SKILL_LEVEL_1
import ru.rknrl.dto.SkillType.ATTACK
import ru.rknrl.dto.SlotId.{SLOT_1, SLOT_3}
import ru.rknrl.dto._

class AccountTest extends AccountTestSpec {
  val config = configMock()
  val accountState = accountStateMock(gold = 1000)

  multi("BuyBuilding", {
    check(
      expectedAccountState = accountState.buyBuilding(SLOT_1, TOWER, config.account),
      clientMessage = BuyBuilding(BuyBuildingDTO(SLOT_1, TOWER)),
      statMessage = Statistics.buyBuilding(TOWER, LEVEL_1)
    )
  })

  multi("UpgradeBuilding", {
    check(
      expectedAccountState = accountState.upgradeBuilding(SLOT_3, config.account),
      clientMessage = UpgradeBuilding(UpgradeBuildingDTO(SLOT_3)),
      statMessage = Statistics.buyBuilding(HOUSE, LEVEL_2)
    )
  })

  multi("RemoveBuilding", {
    check(
      expectedAccountState = accountState.removeBuilding(SLOT_3),
      clientMessage = RemoveBuilding(RemoveBuildingDTO(SLOT_3)),
      statMessage = StatAction.REMOVE_BUILDING
    )
  })

  multi("UpgradeSkill", {
    check(
      expectedAccountState = accountState.upgradeSkill(ATTACK, config.account),
      clientMessage = UpgradeSkill(UpgradeSkillDTO(ATTACK)),
      statMessage = Statistics.buySkill(ATTACK, SKILL_LEVEL_1)
    )
  })

  multi("BuyItem", {
    check(
      expectedAccountState = accountState.buyItem(FIREBALL, config.account),
      clientMessage = BuyItem(BuyItemDTO(FIREBALL)),
      statMessage = Statistics.buyItem(FIREBALL)
    )
  })

  def check(expectedAccountState: AccountState,
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

    database.expectMsg(UpdateAccountState(accountId, expectedAccountState.dto))
    graphite.expectMsg(statMessage)
    database.send(account, AccountStateResponse(Some(expectedAccountState.dto)))

    client.expectMsg(AccountStateUpdated(expectedAccountState.dto))
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

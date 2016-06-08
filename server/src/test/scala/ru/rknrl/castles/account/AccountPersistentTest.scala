//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account

import akka.actor.PoisonPill
import akka.testkit.TestProbe
import protos._
import ru.rknrl.castles.database.Database
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.castles.matchmaking.MatchMaking._
import ru.rknrl.castles.matchmaking.Top

class AccountPersistentTest extends AccountTestSpec {

  "DuplicateAccount" should {
    multi("Если клиент авторизовался - Стопается после получения DuplicateAccount", {
      val secretChecker = new TestProbe(system)
      val database = new TestProbe(system)
      val graphite = new TestProbe(system)
      val client = new TestProbe(system)
      val matchmaking = new TestProbe(system)
      val account = newAccount(
        secretChecker = secretChecker.ref,
        database = database.ref,
        graphite = graphite.ref,
        matchmaking = matchmaking.ref
      )
      authorize(
        secretChecker = secretChecker,
        matchmaking = matchmaking,
        database = database,
        graphite = graphite,
        client = client,
        account = account
      )
      account ! DuplicateAccount
      client.expectMsg(PoisonPill)
    })

    multi("Если клиент НЕ авторизовался - игнорируется", {
      val account = newAccount()
      account ! DuplicateAccount
      watch(account)
      expectNoMsg(noMsgTimeout)
    })
  }

  "UpdateStatistics" should {
    multi("StatAction форвардится базе данных", {
      val secretChecker = new TestProbe(system)
      val database = new TestProbe(system)
      val graphite = new TestProbe(system)
      val client = new TestProbe(system)
      val matchmaking = new TestProbe(system)
      val account = newAccount(
        secretChecker = secretChecker.ref,
        database = database.ref,
        graphite = graphite.ref,
        matchmaking = matchmaking.ref
      )
      authorize(
        secretChecker = secretChecker,
        matchmaking = matchmaking,
        database = database,
        graphite = graphite,
        client = client,
        account = account
      )
      client.send(account, protos.Stat(StatAction.TUTOR_BIG_TOWER))
      graphite.expectMsg(StatAction.TUTOR_BIG_TOWER)
    })

    multi("Если клиент НЕ авторизовался - игнорируется", {
      val account = newAccount()
      account ! protos.Stat(StatAction.TUTOR_BIG_TOWER)
      expectNoMsg(noMsgTimeout)
    })
  }

  "UpdateTutorState" should {
    multi("UpdateTutorState форвардится базе данных", {
      val secretChecker = new TestProbe(system)
      val database = new TestProbe(system)
      val graphite = new TestProbe(system)
      val client = new TestProbe(system)
      val matchmaking = new TestProbe(system)
      val account = newAccount(
        secretChecker = secretChecker.ref,
        database = database.ref,
        graphite = graphite.ref,
        matchmaking = matchmaking.ref
      )
      val accountId = authenticateMock().userInfo.accountId
      authorize(
        secretChecker = secretChecker,
        matchmaking = matchmaking,
        database = database,
        graphite = graphite,
        client = client,
        account = account
      )
      val newTutorState = TutorState(emptySlot = Some(true))
      client.send(account, newTutorState)
      database.expectMsg(Database.UpdateTutorState(accountId, newTutorState))
    })

    multi("Если клиент НЕ авторизовался - игнорируется", {
      val account = newAccount()
      val newTutorState = TutorState(emptySlot = Some(true))
      account ! newTutorState
      expectNoMsg(noMsgTimeout)
    })
  }

  "Offline" should {
    multi("Если клиент авторизовался - Offline отправляется перед остановкой", {
      val client = new TestProbe(system)
      val secretChecker = new TestProbe(system)
      val database = new TestProbe(system)
      val account = newAccount(secretChecker = secretChecker.ref, database = database.ref)

      val authenticate = authenticateMock()
      client.send(account, authenticate)
      secretChecker.expectMsg(authenticate)

      system stop account
      expectMsg(Offline(authenticate.userInfo.accountId, client.ref))
    })

    multi("Если клиент НЕ авторизовался - Offline НЕ отправляется перед остановкой", {
      val account = newAccount()
      system stop account
      expectNoMsg(noMsgTimeout)
    })

  }

  multi("AccountStateResponse", {
    val secretChecker = new TestProbe(system)
    val database = new TestProbe(system)
    val graphite = new TestProbe(system)
    val client = new TestProbe(system)
    val matchmaking = new TestProbe(system)
    val config = configMock()
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
      config = config
    )
    val newState = accountStateMock(gold = 777)
    matchmaking.send(account, Database.AccountStateUpdated(accountId, newState))
    client.expectMsg(newState)
  })

  multi("AccountStateAndRatingResponse", {
    val secretChecker = new TestProbe(system)
    val database = new TestProbe(system)
    val graphite = new TestProbe(system)
    val client = new TestProbe(system)
    val matchmaking = new TestProbe(system)
    val config = configMock()
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
      config = config
    )
    val newState = accountStateMock(gold = 777)
    val top = Top(List.empty, 1)
    matchmaking.send(account, Database.AccountStateAndRatingResponse(accountId, newState, 1666, 3, top))
    client.expectMsg(newState)
    client.expectMsg(Place(3))
    client.expectMsg(top.dto)
  })

}

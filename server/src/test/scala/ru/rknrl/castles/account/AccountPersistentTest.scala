//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account

import akka.testkit.TestProbe
import ru.rknrl.castles.database.Database
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.castles.matchmaking.MatchMaking._
import ru.rknrl.castles.rmi.B2C.AccountStateUpdated
import ru.rknrl.castles.rmi.C2B
import ru.rknrl.castles.rmi.C2B.{Authenticate, UpdateStatistics}
import ru.rknrl.core.rmi.CloseConnection
import ru.rknrl.dto.{StatAction, StatDTO, TutorStateDTO}

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
      client.expectMsg(CloseConnection)
    })

    multi("Если клиент НЕ авторизовался - игнорируется", {
      val account = newAccount()
      account ! DuplicateAccount
      watch(account)
      expectNoMsg()
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
      client.send(account, UpdateStatistics(StatDTO(StatAction.TUTOR_BIG_TOWER)))
      graphite.expectMsg(StatAction.TUTOR_BIG_TOWER)
    })

    multi("Если клиент НЕ авторизовался - игнорируется", {
      val account = newAccount()
      account ! UpdateStatistics(StatDTO(StatAction.TUTOR_BIG_TOWER))
      expectNoMsg()
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
      val newTutorState = TutorStateDTO(emptySlot = Some(true))
      client.send(account, C2B.UpdateTutorState(newTutorState))
      database.expectMsg(Database.UpdateTutorState(accountId, newTutorState))
    })

    multi("Если клиент НЕ авторизовался - игнорируется", {
      val account = newAccount()
      val newTutorState = TutorStateDTO(emptySlot = Some(true))
      account ! C2B.UpdateTutorState(newTutorState)
      expectNoMsg()
    })
  }

  "Offline" should {
    multi("Если клиент авторизовался - Offline отправляется перед остановкой", {
      val client = new TestProbe(system)
      val secretChecker = new TestProbe(system)
      val database = new TestProbe(system)
      val account = newAccount(secretChecker = secretChecker.ref, database = database.ref)

      val authenticate = authenticateMock()
      client.send(account, Authenticate(authenticate))
      secretChecker.expectMsg(authenticate)

      system stop account
      expectMsg(Offline(authenticate.userInfo.accountId, client.ref))
    })

    multi("Если клиент НЕ авторизовался - Offline НЕ отправляется перед остановкой", {
      val account = newAccount()
      system stop account
      expectNoMsg()
    })

  }

  multi("SetAccountState", {
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
    matchmaking.send(account, SetAccountState(accountId, newState))
    client.expectMsg(AccountStateUpdated(newState))
  })

}

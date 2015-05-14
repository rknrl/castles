//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.database

import akka.actor.{ActorRef, Props}
import akka.testkit.TestProbe
import ru.rknrl.castles.kit.Mocks.configMock
import ru.rknrl.dto.AccountId
import ru.rknrl.dto.AccountType.DEV
import ru.rknrl.test.ActorsTest

class AccountsTest extends ActorsTest {
  var iterator = 0

  def newAccounts(database: ActorRef, queue: ActorRef) = {
    iterator += 1
    system.actorOf(Props(classOf[Accounts], database, queue), "accounts-" + iterator)
  }

  multi("Get", {
    val database = new TestProbe(system)
    val queue = new TestProbe(system)
    val accounts = newAccounts(database = database.ref, queue = queue.ref)

    def getAccount(client: TestProbe, accountId: AccountId) = {
      client.send(accounts, Accounts.Get(accountId, client.ref))
      database.expectMsg(Database.GetAccountState(accountId))
      database.send(accounts, Database.AccountStateResponse(accountId, None))
      client.expectMsg(Database.AccountStateResponse(accountId, None))
    }

    val client1 = new TestProbe(system)
    getAccount(client1, AccountId(DEV, "1"))
    getAccount(client1, AccountId(DEV, "1"))
    getAccount(client1, AccountId(DEV, "2"))
    val client2 = new TestProbe(system)
    getAccount(client2, AccountId(DEV, "2"))
  })

  multi("GetAndUpdate", {
    val config = configMock()
    val database = new TestProbe(system)
    val queue = new TestProbe(system)
    val accounts = newAccounts(database = database.ref, queue = queue.ref)

    def getAccount(client: TestProbe, accountId: AccountId) = {
      val newState = config.account.initAccount.addGold(10).dto
      client.send(accounts, Accounts.GetAndUpdate(accountId, client.ref, state ⇒ newState))
      database.expectMsg(Database.GetAccountState(accountId))
      database.send(accounts, Database.AccountStateResponse(accountId, None))
      database.expectMsg(Database.UpdateAccountState(accountId, newState))
      database.send(accounts, Database.AccountStateUpdated(accountId, Some(newState))) // todo remove Some
      client.expectMsg(Database.AccountStateUpdated(accountId, Some(newState)))
    }

    val client1 = new TestProbe(system)
    getAccount(client1, AccountId(DEV, "1"))
    getAccount(client1, AccountId(DEV, "1"))
    getAccount(client1, AccountId(DEV, "2"))
    val client2 = new TestProbe(system)
    getAccount(client2, AccountId(DEV, "2"))
  })

  multi("Get & IsReady", {
    val database = new TestProbe(system)
    val queue = new TestProbe(system)
    val accounts = newAccounts(database = database.ref, queue = queue.ref)
    val client = new TestProbe(system)
    val accountId1 = AccountId(DEV, "1")
    val accountId2 = AccountId(DEV, "2")

    client.send(accounts, Accounts.IsReady(accountId1))
    client.expectMsg(Accounts.Ready(accountId1))

    client.send(accounts, Accounts.Get(accountId1, client.ref))

    client.send(accounts, Accounts.IsReady(accountId1))
    client.expectNoMsg()

    client.send(accounts, Accounts.IsReady(accountId2))
    client.expectMsg(Accounts.Ready(accountId2))
  })

  multi("GetAndUpdate & IsReady", {
    val config = configMock()
    val database = new TestProbe(system)
    val queue = new TestProbe(system)
    val accounts = newAccounts(database = database.ref, queue = queue.ref)
    val client = new TestProbe(system)
    val accountId1 = AccountId(DEV, "1")
    val accountId2 = AccountId(DEV, "2")

    client.send(accounts, Accounts.IsReady(accountId1))
    client.expectMsg(Accounts.Ready(accountId1))

    client.send(accounts, Accounts.GetAndUpdate(accountId1, client.ref, state ⇒ config.account.initAccount.dto))

    client.send(accounts, Accounts.IsReady(accountId1))
    client.expectNoMsg()

    client.send(accounts, Accounts.IsReady(accountId2))
    client.expectMsg(Accounts.Ready(accountId2))
  })
}

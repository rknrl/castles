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
import ru.rknrl.castles.database.Accounts.Ready
import ru.rknrl.castles.kit.Mocks.configMock
import ru.rknrl.dto.AccountId
import ru.rknrl.dto.AccountType.DEV
import ru.rknrl.test.ActorsTest

class AccountsQueueTest extends ActorsTest {
  var iterator = 0

  def newQueue(accounts: ActorRef) = {
    iterator += 1
    system.actorOf(Props(classOf[AccountsQueue], accounts), "accounts-queue-" + iterator)
  }

  multi("queue", {
    val config = configMock()
    val accounts = new TestProbe(system)
    val queue = newQueue(accounts.ref)
    val accountId1 = AccountId(DEV, "1")
    val accountId2 = AccountId(DEV, "2")

    val account1_msg1 = Accounts.Get(accountId1, system.deadLetters)
    val account1_msg2 = Accounts.GetAndUpdate(accountId1, system.deadLetters, a ⇒ config.account.initAccount.dto)
    val account1_msg3 = Accounts.Get(accountId1, system.deadLetters)

    val account2_msg1 = Accounts.Get(accountId2, system.deadLetters)
    val account2_msg2 = Accounts.GetAndUpdate(accountId2, system.deadLetters, a ⇒ config.account.initAccount.dto)

    queue ! account1_msg1
    accounts.expectMsg(Accounts.IsReady(accountId1))

    queue ! account2_msg1
    accounts.expectMsg(Accounts.IsReady(accountId2))

    queue ! account1_msg2
    accounts.expectMsg(Accounts.IsReady(accountId1))

    queue ! account2_msg2
    accounts.expectMsg(Accounts.IsReady(accountId2))

    queue ! account1_msg3
    accounts.expectMsg(Accounts.IsReady(accountId1))

    accounts.send(queue, Ready(accountId1))
    accounts.expectMsg(account1_msg1)

    accounts.send(queue, Ready(accountId2))
    accounts.expectMsg(account2_msg1)

    accounts.send(queue, Ready(accountId1))
    accounts.expectMsg(account1_msg2)

    accounts.send(queue, Ready(accountId1))
    accounts.expectMsg(account1_msg3)

    accounts.send(queue, Ready(accountId2))
    accounts.expectMsg(account2_msg2)

    accounts.send(queue, Ready(accountId1))
    accounts.expectNoMsg()

    accounts.send(queue, Ready(accountId2))
    accounts.expectNoMsg()
  })
}

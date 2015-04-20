//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account

import akka.actor.Props
import ru.rknrl.castles.kit.ActorsTest
import ru.rknrl.castles.matchmaking.NewMatchmaking.{DuplicateAccount, Offline}

class AccountTest extends ActorsTest {

  var accountIterator = 0

  def newAccount = {
    accountIterator += 1
    system.actorOf(Props(classOf[NewAccount], self), "account-" + accountIterator)
  }

  multi("Стопается после получения DuplicateAccount", {
    val account = newAccount
    account ! DuplicateAccount
    watch(account)
    expectMsgPF(timeout.duration) {
      case Offline(_, _) ⇒ true
    }
    expectTerminated(account)
  })

  multi("Отправляет Offline перед остановкой", {
    val account = newAccount
    system stop account
    expectMsgPF(timeout.duration) {
      case Offline(_, _) ⇒ true
    }
  })

}

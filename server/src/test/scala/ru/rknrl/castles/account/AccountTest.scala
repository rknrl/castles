//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account

import akka.testkit.TestProbe
import ru.rknrl.castles.matchmaking.NewMatchmaking._
import ru.rknrl.dto.PlatformType.CANVAS

class AccountTest extends AccountTestSpec {

  multi("Стопается после получения DuplicateAccount", {
    val account = newAccount()
    account ! DuplicateAccount
    watch(account)
    expectTerminated(account)
  })

  "Offline" should {
    multi("Если клиент авторизовался - Offline отправляется перед остановкой", {
      val client = new TestProbe(system)
      val auth = new TestProbe(system)
      val database = new TestProbe(system)
      val account = newAccount(auth = auth.ref, database = database.ref)

      val authenticate = authenticateMock(platformType = CANVAS)
      client.send(account, authenticate)
      auth.expectMsg(authenticate)

      system stop account
      expectMsg(Offline(authenticate.userInfo.accountId, client.ref))
    })

    multi("Если клиент НЕ авторизовался - Offline НЕ отправляется перед остановкой", {
      val account = newAccount()
      system stop account
      expectNoMsg()
    })

  }
}

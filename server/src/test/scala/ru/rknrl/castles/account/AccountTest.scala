//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account

import akka.testkit.TestProbe
import ru.rknrl.castles.account.auth.Auth.SecretChecked
import ru.rknrl.castles.database.Database
import ru.rknrl.castles.database.Database.{AccountStateResponse, TutorStateResponse}
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.castles.matchmaking.NewMatchmaking._
import ru.rknrl.castles.rmi.B2C.Authenticated
import ru.rknrl.dto.{StatAction, TutorStateDTO}

class AccountTest extends AccountTestSpec {

  "DuplicateAccount" should {
    multi("Если клиент авторизовался - Стопается после получения DuplicateAccount", {
      val account = newAccount()

      val authenticate = authenticateMock()
      val accountId = authenticate.userInfo.accountId
      account ! authenticate

      expectMsg(authenticate)
      account ! SecretChecked(valid = true)

      expectMsg(Database.GetAccountState(accountId))
      expectMsg(StatAction.AUTHENTICATED)
      account ! AccountStateResponse(accountId, accountStateMock().dto)

      expectMsg(Database.GetTutorState(accountId))
      account ! TutorStateResponse(accountId, TutorStateDTO())

      expectMsg(Online(accountId))
      expectMsg(InGame(accountId))
      account ! InGameResponse(gameRef = None, searchOpponents = false, top = List.empty)

      expectMsgClass(classOf[Authenticated])

      account ! DuplicateAccount
      watch(account)
      expectMsg(Offline(authenticate.userInfo.accountId, self))
      expectTerminated(account)
    })

    multi("Если клиент НЕ авторизовался - игнорируется", {
      val account = newAccount()
      account ! DuplicateAccount
      watch(account)
      expectNoMsg()
    })
  }

  "Offline" should {
    multi("Если клиент авторизовался - Offline отправляется перед остановкой", {
      val client = new TestProbe(system)
      val auth = new TestProbe(system)
      val database = new TestProbe(system)
      val account = newAccount(auth = auth.ref, database = database.ref)

      val authenticate = authenticateMock()
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

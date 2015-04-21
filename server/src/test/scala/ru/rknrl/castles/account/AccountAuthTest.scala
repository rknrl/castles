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
import ru.rknrl.core.rmi.CloseConnection
import ru.rknrl.dto.AccountType.VKONTAKTE
import ru.rknrl.dto.PlatformType.CANVAS
import ru.rknrl.dto._

class AccountAuthTest extends AccountTestSpec {
  "auth" should {

    multi("reject", {
      val auth = new TestProbe(system)
      val database = new TestProbe(system)
      val client = new TestProbe(system)
      val account = newAccount(auth = auth.ref, database = database.ref)

      val authenticate = authenticateMock()
      client.send(account, authenticate)

      auth.expectMsg(authenticate)
      auth.send(account, SecretChecked(valid = false))

      client.expectMsg(CloseConnection)

      database.expectMsg(StatAction.NOT_AUTHENTICATED)
    })

    multi("success new account", {

      val config = configMock()
      val auth = new TestProbe(system)
      val database = new TestProbe(system)
      val matchmaking = new TestProbe(system)
      val client = new TestProbe(system)
      val account = newAccount(
        matchmaking = matchmaking.ref,
        auth = auth.ref,
        database = database.ref,
        config = config
      )

      val authenticate = authenticateMock(platformType = CANVAS)
      val accountId = authenticate.userInfo.accountId
      client.send(account, authenticate)

      auth.expectMsg(authenticate)
      auth.send(account, SecretChecked(valid = true))

      database.expectMsg(Database.GetAccountState(accountId))
      database.expectMsg(StatAction.AUTHENTICATED)
      database.send(account, Database.AccountNoExists)

      val initAccountState = config.account.initAccount
      val initTutorState = TutorStateDTO()

      database.expectMsg(Database.Insert(accountId, initAccountState.dto, authenticate.userInfo, initTutorState))
      database.send(account, AccountStateResponse(accountId, initAccountState.dto))

      database.expectMsg(Database.GetTutorState(accountId))
      database.send(account, TutorStateResponse(accountId, initTutorState))

      matchmaking.expectMsg(Online(accountId))
      matchmaking.expectMsg(InGame(accountId))
      matchmaking.send(account, InGameResponse(gameRef = None, searchOpponents = true, top = List.empty))

      client.expectMsg(Authenticated(AuthenticatedDTO(
        initAccountState.dto,
        config.account.dto,
        TopDTO(List.empty),
        config.productsDto(CANVAS, VKONTAKTE),
        initTutorState,
        searchOpponents = true,
        game = None
      )))
    })

    multi("success", {

      val config = configMock()
      val auth = new TestProbe(system)
      val database = new TestProbe(system)
      val matchmaking = new TestProbe(system)
      val client = new TestProbe(system)
      val account = newAccount(
        matchmaking = matchmaking.ref,
        auth = auth.ref,
        database = database.ref,
        config = config
      )

      val authenticate = authenticateMock(platformType = CANVAS)
      val accountId = authenticate.userInfo.accountId
      client.send(account, authenticate)

      auth.expectMsg(authenticate)
      auth.send(account, SecretChecked(valid = true))

      val accountState = accountStateMock()
      val tutorState = TutorStateDTO()

      database.expectMsg(Database.GetAccountState(accountId))
      database.expectMsg(StatAction.AUTHENTICATED)
      database.send(account, AccountStateResponse(accountId, accountState.dto))

      database.expectMsg(Database.GetTutorState(accountId))
      database.send(account, TutorStateResponse(accountId, tutorState))

      matchmaking.expectMsg(Online(accountId))
      matchmaking.expectMsg(InGame(accountId))
      matchmaking.send(account, InGameResponse(gameRef = None, searchOpponents = false, top = List.empty))

      client.expectMsg(Authenticated(AuthenticatedDTO(
        accountState.dto,
        config.account.dto,
        TopDTO(List.empty),
        config.productsDto(CANVAS, VKONTAKTE),
        tutorState,
        searchOpponents = false,
        game = None
      )))
    })

    multi("other", {
      // todo Другие сообщение игнорируются
    })
  }
}

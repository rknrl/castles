//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account

import akka.testkit.TestProbe
import ru.rknrl.castles.account.SecretChecker.SecretChecked
import ru.rknrl.castles.database.DatabaseTransaction.GetAccount
import ru.rknrl.castles.database.{Database, DatabaseTransaction}
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.castles.matchmaking.MatchMaking._
import ru.rknrl.castles.rmi.B2C.Authenticated
import ru.rknrl.castles.rmi.C2B.Authenticate
import ru.rknrl.core.rmi.CloseConnection
import ru.rknrl.dto.AccountType.VKONTAKTE
import ru.rknrl.dto.PlatformType.CANVAS
import ru.rknrl.dto._

class AccountAuthTest extends AccountTestSpec {
  "auth" should {

    multi("reject", {
      val secretChecker = new TestProbe(system)
      val database = new TestProbe(system)
      val graphite = new TestProbe(system)
      val client = new TestProbe(system)
      val account = newAccount(secretChecker = secretChecker.ref, database = database.ref, graphite = graphite.ref)

      val authenticate = authenticateMock()
      client.send(account, Authenticate(authenticate))

      secretChecker.expectMsg(authenticate)
      secretChecker.send(account, SecretChecked(valid = false))

      client.expectMsg(CloseConnection)

      graphite.expectMsg(StatAction.NOT_AUTHENTICATED)
    })

    multi("success new account", {

      val config = configMock()
      val secretChecker = new TestProbe(system)
      val database = new TestProbe(system)
      val graphite = new TestProbe(system)
      val matchmaking = new TestProbe(system)
      val client = new TestProbe(system)
      val account = newAccount(
        matchmaking = matchmaking.ref,
        secretChecker = secretChecker.ref,
        database = database.ref,
        graphite = graphite.ref,
        config = config
      )

      val authenticate = authenticateMock(platformType = CANVAS)
      val accountId = authenticate.userInfo.accountId
      client.send(account, Authenticate(authenticate))

      secretChecker.expectMsg(authenticate)
      secretChecker.send(account, SecretChecked(valid = true))

      database.expectMsg(DatabaseTransaction.GetAccount(accountId))
      database.expectMsg(Database.UpdateUserInfo(accountId, authenticate.userInfo))
      graphite.expectMsg(StatAction.AUTHENTICATED)
      database.send(account, DatabaseTransaction.AccountResponse(accountId, state = None, rating = None, tutorState = None, place = 999))

      val initAccountState = config.account.initState
      val initTutorState = TutorStateDTO()

      graphite.expectMsg(StatAction.FIRST_AUTHENTICATED)
      matchmaking.expectMsg(Online(accountId))
      matchmaking.expectMsg(InGame(accountId))
      matchmaking.send(account, InGameResponse(gameRef = None, searchOpponents = false, top = List.empty))

      client.expectMsg(Authenticated(AuthenticatedDTO(
        initAccountState,
        config.account.dto,
        TopDTO(List.empty),
        PlaceDTO(999),
        config.productsDto(CANVAS, VKONTAKTE),
        initTutorState,
        searchOpponents = true,
        game = None
      )))

      graphite.expectMsg(StatAction.START_TUTOR)
      database.expectMsg(GetAccount(accountId))
      database.send(account, DatabaseTransaction.AccountResponse(accountId, state = None, rating = None, tutorState = None, place = 999))
      matchmaking.expectMsgClass(classOf[GameOrder])
    })

    multi("success", {

      val config = configMock()
      val secretChecker = new TestProbe(system)
      val database = new TestProbe(system)
      val graphite = new TestProbe(system)
      val matchmaking = new TestProbe(system)
      val client = new TestProbe(system)
      val account = newAccount(
        matchmaking = matchmaking.ref,
        secretChecker = secretChecker.ref,
        database = database.ref,
        graphite = graphite.ref,
        config = config
      )

      val authenticate = authenticateMock(platformType = CANVAS)
      val accountId = authenticate.userInfo.accountId
      client.send(account, Authenticate(authenticate))

      secretChecker.expectMsg(authenticate)
      secretChecker.send(account, SecretChecked(valid = true))

      val accountState = accountStateMock()
      val tutorState = TutorStateDTO()
      val rating = config.account.initRating

      database.expectMsg(DatabaseTransaction.GetAccount(accountId))
      database.expectMsg(Database.UpdateUserInfo(accountId, authenticate.userInfo))
      graphite.expectMsg(StatAction.AUTHENTICATED)
      database.send(account, DatabaseTransaction.AccountResponse(accountId, state = Some(accountState), rating = Some(rating), tutorState = Some(tutorState), place = 666))

      matchmaking.expectMsg(Online(accountId))
      matchmaking.expectMsg(InGame(accountId))
      matchmaking.send(account, InGameResponse(gameRef = None, searchOpponents = false, top = List.empty))

      client.expectMsg(Authenticated(AuthenticatedDTO(
        accountState,
        config.account.dto,
        TopDTO(List.empty),
        PlaceDTO(666),
        config.productsDto(CANVAS, VKONTAKTE),
        tutorState,
        searchOpponents = false,
        game = None
      )))
    })

    multi("other", {
      // todo Другие сообщения игнорируются
    })
  }
}

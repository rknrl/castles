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
import protos.AccountType.VKONTAKTE
import protos.PlatformType.CANVAS
import protos._
import ru.rknrl.castles.account.SecretChecker.SecretChecked
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.castles.matchmaking.MatchMaking._
import ru.rknrl.castles.matchmaking.Top
import ru.rknrl.castles.storage.Storage
import ru.rknrl.castles.storage.Storage.GetAccount

class AccountAuthTest extends AccountTestSpec {
  "auth" should {

    multi("reject", {
      val secretChecker = new TestProbe(system)
      val database = new TestProbe(system)
      val graphite = new TestProbe(system)
      val client = new TestProbe(system)
      val account = newAccount(secretChecker = secretChecker.ref, database = database.ref, graphite = graphite.ref)

      val authenticate = authenticateMock()
      client.send(account, authenticate)

      secretChecker.expectMsg(authenticate)
      watch(client.ref)
      secretChecker.send(account, SecretChecked(valid = false))

      expectTerminated(client.ref)

      graphite.expectMsg(StatAction.NOT_AUTHENTICATED)
    })

    multi("success new account", {

      val config = configMock()
      val secretChecker = new TestProbe(system)
      val storage = new TestProbe(system)
      val graphite = new TestProbe(system)
      val matchmaking = new TestProbe(system)
      val client = new TestProbe(system)
      val account = newAccount(
        matchmaking = matchmaking.ref,
        secretChecker = secretChecker.ref,
        database = storage.ref,
        graphite = graphite.ref,
        config = config
      )

      val authenticate = authenticateMock(platformType = CANVAS)
      val accountId = authenticate.userInfo.accountId
      client.send(account, authenticate)

      secretChecker.expectMsg(authenticate)
      secretChecker.send(account, SecretChecked(valid = true))

      storage.expectMsg(Storage.GetAccount(accountId))
      storage.expectMsg(Storage.ReplaceUserInfo(accountId, authenticate.userInfo))
      graphite.expectMsg(StatAction.AUTHENTICATED)
      storage.send(account, Storage.AccountResponse(
        accountId,
        state = None,
        rating = None,
        tutorState = None,
        place = None,
        top = new Top(List.empty, 5),
        lastWeekPlace = None,
        lastWeekTop = new Top(List.empty, 4)
      ))

      val initAccountState = config.account.initState
      val initTutorState = TutorState()

      graphite.expectMsg(StatAction.FIRST_AUTHENTICATED)
      matchmaking.expectMsg(Online(accountId))
      matchmaking.expectMsg(InGame(accountId))
      matchmaking.send(account, InGameResponse(gameRef = None, searchOpponents = false))

      client.expectMsg(Authenticated(
        initAccountState,
        config.account.dto,
        protos.Top(5, List.empty),
        None,
        config.productsDto(CANVAS, VKONTAKTE),
        initTutorState,
        searchOpponents = true,
        game = None,
        lastWeekPlace = None,
        lastWeekTop = Some(protos.Top(4, List.empty))
      ))

      graphite.expectMsg(StatAction.START_TUTOR)
      storage.expectMsg(GetAccount(accountId))
      storage.send(account, Storage.AccountResponse(
        accountId,
        state = None,
        rating = None,
        tutorState = None,
        place = Some(999),
        top = new Top(List.empty, 5),
        lastWeekPlace = Some(666),
        lastWeekTop = new Top(List.empty, 5)
      ))
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
      client.send(account, authenticate)

      secretChecker.expectMsg(authenticate)
      secretChecker.send(account, SecretChecked(valid = true))

      val accountState = accountStateMock(weekNumberAccepted = Some(4))
      val tutorState = TutorState()
      val rating = config.account.initRating

      database.expectMsg(Storage.GetAccount(accountId))
      database.expectMsg(Storage.ReplaceUserInfo(accountId, authenticate.userInfo))
      graphite.expectMsg(StatAction.AUTHENTICATED)
      database.send(account, Storage.AccountResponse(
        accountId,
        state = Some(accountState),
        rating = Some(rating),
        tutorState = Some(tutorState),
        place = Some(666),
        top = new Top(List.empty, 5),
        lastWeekPlace = Some(667),
        lastWeekTop = new Top(List.empty, 4)
      ))

      matchmaking.expectMsg(Online(accountId))
      matchmaking.expectMsg(InGame(accountId))
      matchmaking.send(account, InGameResponse(gameRef = None, searchOpponents = false))

      client.expectMsg(Authenticated(
        accountState,
        config.account.dto,
        protos.Top(5, List.empty),
        Some(Place(666)),
        config.productsDto(CANVAS, VKONTAKTE),
        tutorState,
        searchOpponents = false,
        game = None
        // lastWeekPlace & lastWeekTop не отправляем, потому что игрок их уже видел
      ))
    })

    multi("other", {
      // todo Другие сообщения игнорируются
    })
  }
}

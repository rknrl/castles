//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account

import akka.actor.ActorRef
import akka.testkit.TestProbe
import protos.AccountType.VKONTAKTE
import protos.PlatformType.CANVAS
import protos._
import ru.rknrl.castles.Config
import ru.rknrl.castles.account.SecretChecker.SecretChecked
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.castles.matchmaking.MatchMaking.{InGame, InGameResponse, Online}
import ru.rknrl.castles.matchmaking.Top
import ru.rknrl.castles.storage.Storage.GetAccount
import ru.rknrl.castles.storage.{Calendar, FakeCalendar, Storage}
import ru.rknrl.test.ActorsTest

class AccountTestSpec extends ActorsTest {

  var accountIterator = 0

  def newAccount(matchmaking: ActorRef = self,
                 secretChecker: ActorRef = self,
                 database: ActorRef = self,
                 graphite: ActorRef = self,
                 config: Config = configMock(),
                 calendar: Calendar = new FakeCalendar(week = 3)) = {
    accountIterator += 1
    system.actorOf(
      Account.props(
        matchmaking = matchmaking,
        secretChecker = secretChecker,
        storage = database,
        graphite = graphite,
        config = config,
        calendar = calendar
      ),
      "account-" + accountIterator
    )
  }

  def authenticateMock(userInfo: UserInfo = UserInfo(AccountId(VKONTAKTE, "1")),
                       secret: AuthenticationSecret = AuthenticationSecret(body = "body"),
                       platformType: PlatformType = PlatformType.CANVAS,
                       deviceType: DeviceType = DeviceType.PC) =
    Authenticate(userInfo, platformType, deviceType, secret)

  def authorize(config: Config = configMock(),
                secretChecker: TestProbe,
                database: TestProbe,
                graphite: TestProbe,
                matchmaking: TestProbe,
                client: TestProbe,
                account: ActorRef,
                accountState: AccountState = accountStateMock()) = {

    val authenticate = authenticateMock(platformType = CANVAS)
    val accountId = authenticate.userInfo.accountId
    client.send(account, authenticate)

    secretChecker.expectMsg(authenticate)
    secretChecker.send(account, SecretChecked(valid = true))

    val tutorState = TutorState()
    val rating = config.account.initRating

    database.expectMsg(GetAccount(accountId))
    database.expectMsg(Storage.UpdateUserInfo(accountId, authenticate.userInfo))
    graphite.expectMsg(StatAction.AUTHENTICATED)
    database.send(account, Storage.AccountResponse(
      accountId,
      state = Some(accountState),
      rating = Some(rating),
      tutorState = Some(tutorState),
      top = new Top(List.empty, 5),
      place = Some(777),
      lastWeekPlace = Some(666),
      lastWeekTop = new Top(List.empty, 4)
    ))

    matchmaking.expectMsg(Online(accountId))
    matchmaking.expectMsg(InGame(accountId))
    matchmaking.send(account, InGameResponse(gameRef = None, searchOpponents = false))

    client.expectMsg(Authenticated(
      accountState,
      config.account.dto,
      protos.Top(5, List.empty),
      Some(Place(777)),
      config.productsDto(CANVAS, VKONTAKTE),
      tutorState,
      searchOpponents = false,
      game = None,
      lastWeekPlace = Some(Place(666)),
      lastWeekTop = Some(protos.Top(4, List.empty))
    ))
  }
}

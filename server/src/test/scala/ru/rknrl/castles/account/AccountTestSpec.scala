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
import ru.rknrl.castles.Config
import ru.rknrl.castles.account.SecretChecker.SecretChecked
import ru.rknrl.castles.database.Database
import ru.rknrl.castles.database.Database._
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.castles.matchmaking.MatchMaking.{InGame, InGameResponse, Online}
import ru.rknrl.castles.rmi.B2C.Authenticated
import ru.rknrl.castles.rmi.C2B.Authenticate
import ru.rknrl.dto.AccountType.VKONTAKTE
import ru.rknrl.dto.PlatformType.CANVAS
import ru.rknrl.dto._
import ru.rknrl.test.ActorsTest

class AccountTestSpec extends ActorsTest {

  var accountIterator = 0

  def newAccount(matchmaking: ActorRef = self,
                 secretChecker: ActorRef = self,
                 database: ActorRef = self,
                 graphite: ActorRef = self,
                 config: Config = configMock()) = {
    accountIterator += 1
    system.actorOf(
      Account.props(
        matchmaking = matchmaking,
        secretChecker = secretChecker,
        databaseQueue = database,
        graphite = graphite,
        config = config
      ),
      "account-" + accountIterator
    )
  }

  def authenticateMock(userInfo: UserInfoDTO = UserInfoDTO(AccountId(VKONTAKTE, "1")),
                       secret: AuthenticationSecretDTO = AuthenticationSecretDTO(body = "body"),
                       platformType: PlatformType = PlatformType.CANVAS,
                       deviceType: DeviceType = DeviceType.PC) =
    AuthenticateDTO(userInfo, platformType, deviceType, secret)

  def authorize(config: Config = configMock(),
                secretChecker: TestProbe,
                database: TestProbe,
                graphite: TestProbe,
                matchmaking: TestProbe,
                client: TestProbe,
                account: ActorRef,
                accountState: AccountStateDTO = accountStateMock()) = {

    val authenticate = authenticateMock(platformType = CANVAS)
    val accountId = authenticate.userInfo.accountId
    client.send(account, Authenticate(authenticate))

    secretChecker.expectMsg(authenticate)
    secretChecker.send(account, SecretChecked(valid = true))

    val tutorState = TutorStateDTO()
    val rating = config.account.initRating

    database.expectMsg(GetAccount(accountId))
    database.expectMsg(Database.UpdateUserInfo(accountId, authenticate.userInfo))
    graphite.expectMsg(StatAction.AUTHENTICATED)
    database.send(account, AccountResponse(accountId, state = Some(accountState), rating = Some(rating), tutorState = Some(tutorState), place = 777))

    matchmaking.expectMsg(Online(accountId))
    matchmaking.expectMsg(InGame(accountId))
    matchmaking.send(account, InGameResponse(gameRef = None, searchOpponents = false, top = List.empty))

    client.expectMsg(Authenticated(AuthenticatedDTO(
      accountState,
      config.account.dto,
      TopDTO(List.empty),
      PlaceDTO(777),
      config.productsDto(CANVAS, VKONTAKTE),
      tutorState,
      searchOpponents = false,
      game = None
    )))
  }
}

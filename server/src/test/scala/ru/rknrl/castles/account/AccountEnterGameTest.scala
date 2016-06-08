//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account

import akka.testkit.TestProbe
import protos._
import ru.rknrl.castles.database.Database
import ru.rknrl.castles.database.Database.GetAccount
import ru.rknrl.castles.game.Game.Join
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.castles.matchmaking.MatchMaking.{AccountLeaveGame, ConnectToGame, GameOrder}
import ru.rknrl.castles.matchmaking.Top

class AccountEnterGameTest extends AccountTestSpec {
  val config = configMock()
  val accountState = accountStateMock(gold = 1000)

  multi("ConnectToGame", {
    val secretChecker = new TestProbe(system)
    val database = new TestProbe(system)
    val graphite = new TestProbe(system)
    val client = new TestProbe(system)
    val matchmaking = new TestProbe(system)
    val account = newAccount(
      secretChecker = secretChecker.ref,
      database = database.ref,
      graphite = graphite.ref,
      matchmaking = matchmaking.ref,
      config = config
    )
    val accountId = authenticateMock().userInfo.accountId
    authorize(
      secretChecker = secretChecker,
      matchmaking = matchmaking,
      database = database,
      graphite = graphite,
      client = client,
      account = account,
      config = config,
      accountState = accountState
    )

    client.send(account, EnterGame)

    database.expectMsg(GetAccount(accountId))
    database.send(account, Database.AccountResponse(
      accountId,
      state = Some(accountState),
      rating = Some(config.account.initRating),
      tutorState = None,
      place = Some(999),
      top = new Top(List.empty, 5),
      lastWeekPlace = Some(666),
      lastWeekTop = new Top(List.empty, 4)
    ))

    matchmaking.expectMsg(
      GameOrder(
        accountId,
        authenticateMock().deviceType,
        authenticateMock().userInfo,
        accountState,
        rating = config.account.initRating,
        isBot = false
      )
    )

    val game = new TestProbe(system)
    matchmaking.send(account, ConnectToGame(game.ref))
    client.expectMsg(EnteredGame(NodeLocator(config.host, config.gamePort)))
    client.send(account, JoinGame())
    game.expectMsg(Join(accountId, client.ref))

    // game

    val cast = CastFireball(PointDTO(20, 30))
    client.send(account, cast)
    game.expectMsg(cast)

    client.send(account, protos.Stat(StatAction.TUTOR_BIG_TOWER))
    game.expectMsg(StatAction.TUTOR_BIG_TOWER)
    graphite.expectMsg(StatAction.TUTOR_BIG_TOWER)

    matchmaking.send(account, AccountLeaveGame)
    client.expectMsg(LeavedGame())
  })

}

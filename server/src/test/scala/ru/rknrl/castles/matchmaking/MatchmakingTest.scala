//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.matchmaking

import akka.actor._
import akka.testkit.TestProbe
import protos.AccountType.VKONTAKTE
import protos.StatAction.START_GAME_4_WITH_BOTS
import protos._
import ru.rknrl.castles.database.DatabaseTransaction.AccountStateResponse
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.castles.matchmaking.MatchMaking._

import scala.concurrent.duration._

class MatchmakingTest extends MatchmakingTestSpec {

  multi("Два актора отправляют PlaceGameOrder - оба получают ConnectToGame", {
    val database = new TestProbe(system)
    val graphite = new TestProbe(system)
    val matchmaking = newMatchmaking(database = database.ref, graphite = graphite.ref)
    val accountId1 = AccountId(VKONTAKTE, "1")
    val accountId2 = AccountId(VKONTAKTE, "2")

    val client1 = new TestProbe(system)
    client1.send(matchmaking, Online(accountId1))
    client1.send(matchmaking, newGameOrder(accountId1))

    val client2 = new TestProbe(system)
    client2.send(matchmaking, Online(accountId2))
    client2.send(matchmaking, newGameOrder(accountId2))
    client2.send(matchmaking, TryCreateGames)

    graphite.expectMsg(10 seconds, StatAction.START_GAME_4_WITH_PLAYERS)

    client1.expectMsgPF(10 seconds) {
      case ConnectToGame(gameRef) ⇒ true
    }
    client1.expectNoMsg(noMsgTimeout)

    client2.expectMsgPF(10 seconds) {
      case ConnectToGame(gameRef) ⇒ true
    }
    client2.expectNoMsg(noMsgTimeout)

    matchmaking ! TryCreateGames
    client1.expectNoMsg(noMsgTimeout)
    client2.expectNoMsg(noMsgTimeout)
  })

  multi("Уже находимся в игре и отправляем еще PlaceGameOrder - получаем ConnectToGame", {
    val database = new TestProbe(system)
    val graphite = new TestProbe(system)
    val client = new TestProbe(system)
    val matchmaking = newMatchmaking(database = database.ref, graphite = graphite.ref)
    val accountId = AccountId(VKONTAKTE, "1")
    var game: Option[ActorRef] = None
    client.send(matchmaking, Online(accountId))
    client.send(matchmaking, newGameOrder(accountId))
    client.send(matchmaking, TryCreateGames)
    graphite.expectMsg(StatAction.START_GAME_4_WITH_BOTS)
    client.expectMsgPF(timeout.duration) {
      case ConnectToGame(gameRef) ⇒ game = Some(gameRef)
    }
    client.send(matchmaking, newGameOrder(accountId))
    client.expectMsgPF(timeout.duration) {
      case ConnectToGame(gameRef) ⇒ gameRef shouldBe game.get
    }
  })

  multi("игра удаляется после AllPlayersLeaveGame", {
    val database = new TestProbe(system)
    val graphite = new TestProbe(system)
    val client = new TestProbe(system)
    val matchmaking = newMatchmaking(database = database.ref, graphite = graphite.ref)

    val accountId = AccountId(VKONTAKTE, "1")

    var game: Option[ActorRef] = None

    client.send(matchmaking, Online(accountId))
    client.send(matchmaking, newGameOrder(accountId))
    client.send(matchmaking, TryCreateGames)
    graphite.expectMsg(StatAction.START_GAME_4_WITH_BOTS)
    client.expectMsgPF(timeout.duration) {
      case ConnectToGame(gameRef) ⇒ game = Some(gameRef)
    }
    client.send(matchmaking, AllPlayersLeaveGame(game.get))
    watch(game.get)
    expectTerminated(game.get)
  })


  multi("AllPlayersLeaveGame удаляет только одну игру", {
    val database = new TestProbe(system)
    val graphite = new TestProbe(system)
    val matchmaking = newMatchmaking(database = database.ref, graphite = graphite.ref)
    val accountId1 = AccountId(VKONTAKTE, "1")
    val accountId2 = AccountId(VKONTAKTE, "2")
    var game1: Option[ActorRef] = None
    var game2: Option[ActorRef] = None

    val client1 = new TestProbe(system)
    client1.send(matchmaking, Online(accountId1))
    client1.send(matchmaking, newGameOrder(accountId1))
    client1.send(matchmaking, TryCreateGames)
    client1.expectMsgPF(timeout.duration) {
      case ConnectToGame(gameRef) ⇒ game1 = Some(gameRef)
    }

    val client2 = new TestProbe(system)
    client2.send(matchmaking, Online(accountId2))
    client2.send(matchmaking, newGameOrder(accountId2))
    client2.send(matchmaking, TryCreateGames)
    client2.expectMsgPF(timeout.duration) {
      case ConnectToGame(gameRef) ⇒ game2 = Some(gameRef)
    }

    client2.send(matchmaking, AllPlayersLeaveGame(game1.get))
    client2.send(matchmaking, InGame(accountId2))
    client2.expectMsg(InGameResponse(gameRef = game2, searchOpponents = false))
    client2.expectNoMsg(noMsgTimeout)
  })

  multi("PlayerLeaveGame", {
    val database = new TestProbe(system)
    val graphite = new TestProbe(system)
    val client = new TestProbe(system)
    val matchmaking = newMatchmaking(database = database.ref, graphite = graphite.ref)

    val accountId = AccountId(VKONTAKTE, "1")

    var game: Option[ActorRef] = None

    client.send(matchmaking, Online(accountId))
    client.send(matchmaking, newGameOrder(accountId))
    client.send(matchmaking, TryCreateGames)
    graphite.expectMsg(StatAction.START_GAME_4_WITH_BOTS)
    client.expectMsgPF(timeout.duration) {
      case ConnectToGame(gameRef) ⇒ game = Some(gameRef)
    }
    client.send(matchmaking, PlayerLeaveGame(accountId, place = 1, reward = 2, usedItems = ItemType.values.map(_ → 0).toMap))
    client.expectMsg(AccountLeaveGame)
    client.send(matchmaking, InGame(accountId))
    client.expectMsg(InGameResponse(gameRef = None, searchOpponents = false))
  })


  multi("InGame", {
    val database = new TestProbe(system)
    val graphite = new TestProbe(system)
    val client = new TestProbe(system)
    val matchmaking = newMatchmaking(database = database.ref, graphite = graphite.ref)

    val accountId = AccountId(VKONTAKTE, "1")

    var game: Option[ActorRef] = None

    client.send(matchmaking, Online(accountId))
    client.send(matchmaking, InGame(accountId))
    client.expectMsgPF(timeout.duration) {
      case InGameResponse(None, false) ⇒ true
    }

    client.send(matchmaking, newGameOrder(accountId))
    client.send(matchmaking, InGame(accountId))
    client.expectMsgPF(timeout.duration) {
      case InGameResponse(None, true) ⇒ true
    }

    client.send(matchmaking, TryCreateGames)
    graphite.expectMsgClass(classOf[StatAction])
    client.expectMsgPF(timeout.duration) {
      case ConnectToGame(gameRef) ⇒ game = Some(gameRef)
    }

    client.send(matchmaking, InGame(accountId))
    client.expectMsgPF(timeout.duration) {
      case InGameResponse(game, false) ⇒ true
    }

    client.send(matchmaking, PlayerLeaveGame(accountId, place = 1, reward = 2, usedItems = ItemType.values.map(_ → 0).toMap))
    client.expectMsg(AccountLeaveGame)
    client.send(matchmaking, InGame(accountId))
    client.expectMsgPF(timeout.duration) {
      case InGameResponse(None, false) ⇒ true
    }
    client.expectNoMsg(noMsgTimeout)
  })

  multi("2 раза Online с одного актора", {
    val matchmaking = newMatchmaking(database = self, graphite = self)

    val accountId = AccountId(VKONTAKTE, "1")

    matchmaking ! Online(accountId)
    matchmaking ! Online(accountId)
    expectNoMsg(noMsgTimeout)
  })

  multi("Offline без Online", {
    val matchmaking = newMatchmaking(database = self, graphite = self)

    val accountId = AccountId(VKONTAKTE, "1")

    matchmaking ! Offline(accountId, self)
    expectNoMsg(noMsgTimeout)
  })

  multi("Offline", {
    val database = new TestProbe(system)
    val graphite = new TestProbe(system)
    val client = new TestProbe(system)
    val matchmaking = newMatchmaking(database = database.ref, graphite = graphite.ref)
    val accountId = AccountId(VKONTAKTE, "1")
    client.send(matchmaking, Online(accountId))
    client.send(matchmaking, newGameOrder(accountId))
    client.send(matchmaking, Offline(accountId, self))
    matchmaking ! TryCreateGames
    graphite.expectMsg(START_GAME_4_WITH_BOTS)
    client.expectNoMsg(noMsgTimeout)
  })

  multi("Matchmaking форвадит Offline to Game если не тутор", {
    val database = new TestProbe(system)
    val graphite = new TestProbe(system)
    val client = new TestProbe(system)
    val matchmaking = newMatchmaking(gameFactory = new FakeGameFactory(self), database = database.ref, graphite = graphite.ref)
    val accountId = AccountId(VKONTAKTE, "1")
    client.send(matchmaking, Online(accountId))
    client.send(matchmaking, newGameOrder(accountId))
    matchmaking ! TryCreateGames
    graphite.expectMsgClass(classOf[StatAction])
    client.expectMsgPF(timeout.duration) {
      case ConnectToGame(gameRef) ⇒ true
    }
    client.send(matchmaking, Offline(accountId, client.ref))
    expectMsgPF(timeout.duration) {
      case Offline(id, client) ⇒ id shouldBe accountId
    }
    expectNoMsg(noMsgTimeout)
  })

  multi("Matchmaking получает Offline и убивает игру если тутор", {
    val database = new TestProbe(system)
    val graphite = new TestProbe(system)
    val client = new TestProbe(system)
    val matchmaking = newMatchmaking(database = database.ref, graphite = graphite.ref)
    val accountId = AccountId(VKONTAKTE, "1")
    client.send(matchmaking, Online(accountId))
    client.send(matchmaking, newGameOrder(accountId, accountState = accountStateMock(gamesCount = 0)))
    matchmaking ! TryCreateGames
    var game: Option[ActorRef] = None
    client.expectMsgPF(timeout.duration) {
      case ConnectToGame(gameRef) ⇒ game = Some(gameRef)
    }
    client.send(matchmaking, Offline(accountId, client.ref))
    watch(game.get)
    expectTerminated(game.get)
  })

  multi("Double Online", {
    val matchmaking = newMatchmaking(database = self, graphite = self)

    val accountId = AccountId(VKONTAKTE, "1")

    val client1 = new TestProbe(system)
    val client2 = new TestProbe(system)

    client1.send(matchmaking, Online(accountId))
    client1.send(matchmaking, newGameOrder(accountId))

    client2.send(matchmaking, Online(accountId))
    client2.send(matchmaking, newGameOrder(accountId))

    client1.expectMsg(DuplicateAccount)
    client1.expectNoMsg(noMsgTimeout)

    client2.send(matchmaking, TryCreateGames)
    client2.expectMsgClass(classOf[ConnectToGame])
    client2.expectNoMsg(noMsgTimeout)
  })

  multi("Database.Response forward to Account", {
    val matchmaking = newMatchmaking(database = self, graphite = self)

    val accountId1 = AccountId(VKONTAKTE, "1")
    val accountId2 = AccountId(VKONTAKTE, "2")

    val client1 = new TestProbe(system)
    val client2 = new TestProbe(system)

    client1.send(matchmaking, Online(accountId1))
    client2.send(matchmaking, Online(accountId2))

    val msg = AccountStateResponse(accountId1, accountStateMock())
    matchmaking ! msg

    client1.expectMsg(msg)
    client1.expectNoMsg(noMsgTimeout)

    client2.expectNoMsg(noMsgTimeout)
  })

  // todo supervision
  // todo playerLeaveGame
}
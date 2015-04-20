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
import ru.rknrl.castles.kit.ActorsTest
import ru.rknrl.castles.matchmaking.NewMatchmaking._
import ru.rknrl.dto.AccountId
import ru.rknrl.dto.AccountType.VKONTAKTE

class MatchmakingTest extends ActorsTest {

  var matchmakingIterator = 0

  def newMatchmaking = {
    matchmakingIterator += 1
    system.actorOf(Props(classOf[NewMatchmaking], new GameFactory), "matchmaking-" + matchmakingIterator)
  }

  def newMatchmakingWithSelfAsGameFactory = {
    matchmakingIterator += 1
    system.actorOf(Props(classOf[NewMatchmaking], new FakeGameFactory(self)), "matchmaking-" + matchmakingIterator)
  }

  multi("Два актора отправляют PlaceGameOrderNew - оба получают ConnectToGame", {
    val matchmaking = newMatchmaking
    val accountId1 = AccountId(VKONTAKTE, "1")
    val accountId2 = AccountId(VKONTAKTE, "2")

    val client1 = new TestProbe(system)
    client1.send(matchmaking, Online(accountId1))
    client1.send(matchmaking, GameOrderNew(accountId1))

    val client2 = new TestProbe(system)
    client2.send(matchmaking, Online(accountId2))
    client2.send(matchmaking, GameOrderNew(accountId2))
    client2.send(matchmaking, TryCreateGames)

    client1.expectMsgPF(timeout.duration) {
      case ConnectToGame(gameRef) ⇒ true
    }
    client1.expectNoMsg()

    client2.expectMsgPF(timeout.duration) {
      case ConnectToGame(gameRef) ⇒ true
    }
    client2.expectNoMsg()

    matchmaking ! TryCreateGames
    client1.expectNoMsg()
    client2.expectNoMsg()
  })

  multi("Уже находимся в игре и отправляем еще PlaceGameOrderNew - получаем ConnectToGame", {
    val matchmaking = newMatchmaking
    val accountId = AccountId(VKONTAKTE, "1")
    var game: Option[ActorRef] = None
    matchmaking ! Online(accountId)
    matchmaking ! GameOrderNew(accountId)
    matchmaking ! TryCreateGames
    expectMsgPF(timeout.duration) {
      case ConnectToGame(gameRef) ⇒ game = Some(gameRef)
    }
    matchmaking ! GameOrderNew(accountId)
    expectMsgPF(timeout.duration) {
      case ConnectToGame(gameRef) ⇒ gameRef shouldBe game.get
    }
  })

  multi("игра удаляется после AllPlayersLeaveGame", {
    val matchmaking = newMatchmaking

    val accountId = AccountId(VKONTAKTE, "1")

    var game: Option[ActorRef] = None

    matchmaking ! Online(accountId)
    matchmaking ! GameOrderNew(accountId)
    matchmaking ! TryCreateGames
    expectMsgPF(timeout.duration) {
      case ConnectToGame(gameRef) ⇒ game = Some(gameRef)
    }
    matchmaking ! AllPlayersLeaveGame(game.get)

    watch(game.get)
    expectTerminated(game.get)
  })

  multi("AllPlayersLeaveGame удаляет только одну игру", {
    val matchmaking = newMatchmaking
    val accountId1 = AccountId(VKONTAKTE, "1")
    val accountId2 = AccountId(VKONTAKTE, "2")
    var game1: Option[ActorRef] = None
    var game2: Option[ActorRef] = None
    val client1 = new TestProbe(system)
    client1.send(matchmaking, Online(accountId1))
    client1.send(matchmaking, GameOrderNew(accountId1))
    client1.send(matchmaking, TryCreateGames)
    client1.expectMsgPF(timeout.duration) {
      case ConnectToGame(gameRef) ⇒ game1 = Some(gameRef)
    }

    val client2 = new TestProbe(system)
    client2.send(matchmaking, Online(accountId2))
    client2.send(matchmaking, GameOrderNew(accountId2))
    client2.send(matchmaking, TryCreateGames)
    client2.expectMsgPF(timeout.duration) {
      case ConnectToGame(gameRef) ⇒ game2 = Some(gameRef)
    }

    client2.send(matchmaking, AllPlayersLeaveGame(game1.get))
    client2.send(matchmaking, InGame(accountId2))
    client2.expectMsgPF(timeout.duration) {
      case InGameResponseNew(gameRef, searchOpponents) ⇒
        gameRef.get shouldBe game2.get
        searchOpponents shouldBe false
    }
    client2.expectNoMsg()
  })

  multi("PlayerLeaveGameNew", {
    val matchmaking = newMatchmaking

    val accountId = AccountId(VKONTAKTE, "1")

    var game: Option[ActorRef] = None

    matchmaking ! Online(accountId)
    matchmaking ! GameOrderNew(accountId)
    matchmaking ! TryCreateGames
    expectMsgPF(timeout.duration) {
      case ConnectToGame(gameRef) ⇒ game = Some(gameRef)
    }
    matchmaking ! PlayerLeaveGameNew(accountId)
    matchmaking ! InGame(accountId)
    expectMsgPF(timeout.duration) {
      case InGameResponseNew(gameRef, searchOpponents) ⇒
        gameRef shouldBe None
        searchOpponents shouldBe false
    }

  })

  multi("InGame", {
    val matchmaking = newMatchmaking

    val accountId = AccountId(VKONTAKTE, "1")

    var game: Option[ActorRef] = None

    matchmaking ! Online(accountId)
    matchmaking ! InGame(accountId)
    expectMsgPF(timeout.duration) {
      case InGameResponseNew(None, false) ⇒ true
    }

    matchmaking ! GameOrderNew(accountId)
    matchmaking ! InGame(accountId)
    expectMsgPF(timeout.duration) {
      case InGameResponseNew(None, true) ⇒ true
    }

    matchmaking ! TryCreateGames
    expectMsgPF(timeout.duration) {
      case ConnectToGame(gameRef) ⇒ game = Some(gameRef)
    }

    matchmaking ! InGame(accountId)
    expectMsgPF(timeout.duration) {
      case InGameResponseNew(game, false) ⇒ true
    }

    matchmaking ! AllPlayersLeaveGame(game.get)
    matchmaking ! InGame(accountId)
    expectMsgPF(timeout.duration) {
      case InGameResponseNew(None, false) ⇒ true
    }

    expectNoMsg()
  })

  multi("2 раза Online с одного актора", {
    val matchmaking = newMatchmaking

    val accountId = AccountId(VKONTAKTE, "1")

    matchmaking ! Online(accountId)
    matchmaking ! Online(accountId)
    expectNoMsg()
  })

  multi("Offline без Online", {
    val matchmaking = newMatchmaking

    val accountId = AccountId(VKONTAKTE, "1")

    matchmaking ! Offline(accountId, self)
    expectNoMsg()
  })

  multi("Offline", {
    val matchmaking = newMatchmaking
    val accountId = AccountId(VKONTAKTE, "1")
    matchmaking ! Online(accountId)
    matchmaking ! GameOrderNew(accountId)
    matchmaking ! Offline(accountId, self)
    matchmaking ! TryCreateGames
    expectNoMsg()
  })

  multi("Matchmaking форвадит Offline to Game", {
    val matchmaking = newMatchmakingWithSelfAsGameFactory
    val accountId = AccountId(VKONTAKTE, "1")
    matchmaking ! Online(accountId)
    matchmaking ! GameOrderNew(accountId)
    matchmaking ! TryCreateGames
    expectMsgPF(timeout.duration) {
      case ConnectToGame(gameRef) ⇒ true
    }
    matchmaking ! Offline(accountId, self)
    expectMsgPF(timeout.duration) {
      case Offline(id, client) ⇒ id shouldBe accountId
    }
    expectNoMsg()
  })

  multi("Double Online", {
    val matchmaking = newMatchmaking

    val accountId = AccountId(VKONTAKTE, "1")

    val client1 = new TestProbe(system)
    val client2 = new TestProbe(system)

    client1.send(matchmaking, Online(accountId))
    client1.send(matchmaking, GameOrderNew(accountId))

    client2.send(matchmaking, Online(accountId))
    client2.send(matchmaking, GameOrderNew(accountId))

    client1.expectMsg(DuplicateAccount)
    client1.expectNoMsg()

    client2.send(matchmaking, TryCreateGames)
    client2.expectMsgClass(classOf[ConnectToGame])
    client2.expectNoMsg()
  })
}
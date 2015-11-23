//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.game

import akka.testkit.TestProbe
import protos.AccountType.{FACEBOOK, VKONTAKTE}
import protos._
import ru.rknrl.castles.game.Game.{Join, UpdateGameState}
import ru.rknrl.castles.matchmaking.MatchMaking.{AllPlayersLeaveGame, PlayerLeaveGame}

class GameLeaveTest extends GameTestSpec {
  multi("Leave", {

    // Создаем игру на двух игроков

    val game = newGame(gameState = initGameState)

    val client0 = new TestProbe(system)
    val client1 = new TestProbe(system)

    // Игроки посылают Join

    client0.send(game, Join(AccountId(VKONTAKTE, "1"), client0.ref))
    client1.send(game, Join(AccountId(FACEBOOK, "1"), client1.ref))

    // Игроки получают в ответ JoinedGame с актуальным геймстейтом

    client0.expectMsgClass(classOf[GameState])

    client1.expectMsgClass(classOf[GameState])

    // Если отправить LeaveGame раньше Surrender - они игнорируются

    client0.send(game, LeaveGame())
    client1.send(game, LeaveGame())
    client0.expectNoMsg(noMsgTimeout)
    client1.expectNoMsg(noMsgTimeout)
    expectNoMsg(noMsgTimeout)

    // Игроки отправляют Surrender и получают в ответ GameOver

    client0.send(game, Surrender())

    client0.expectMsgClass(classOf[GameOver])
    client0.expectMsgClass(classOf[GameOver])

    client1.expectMsgClass(classOf[GameOver])
    client1.expectMsgClass(classOf[GameOver])

    // Если отправить LeaveGame с невалидного адреса - он игнорируется

    val client3 = new TestProbe(system)
    client3.send(game, LeaveGame())
    client0.expectNoMsg(noMsgTimeout)
    client1.expectNoMsg(noMsgTimeout)
    expectNoMsg(noMsgTimeout)

    // Игроки отправляют LeaveGame
    // Матчмайкинг получает PlayerLeaveGame

    client0.send(game, LeaveGame())
    client0.expectNoMsg(noMsgTimeout)
    expectMsgPF(TIMEOUT) {
      case PlayerLeaveGame(accountId, place, reward, usedItems) ⇒
        accountId shouldBe AccountId(VKONTAKTE, "1")
        place shouldBe 2
        reward shouldBe 0
        usedItems shouldBe ItemType.values.map(_ → 0).toMap
    }

    expectNoMsg(noMsgTimeout) // Матчмайкинг НЕ получает AllPlayersLeaveGame раньше чем надо

    client1.send(game, LeaveGame())
    client1.expectNoMsg(noMsgTimeout)
    expectMsgPF(TIMEOUT) {
      case PlayerLeaveGame(accountId, place, reward, usedItems) ⇒
        accountId shouldBe AccountId(FACEBOOK, "1")
        place shouldBe 1
        reward shouldBe 2
        usedItems shouldBe ItemType.values.map(_ → 0).toMap
    }

    // Матчмайкинг получает AllPlayersLeaveGame

    expectMsg(AllPlayersLeaveGame(game))

    // Если еще раз отправить LeaveGame они будут игнорироваться
    client0.send(game, LeaveGame())
    client1.send(game, LeaveGame())
    client0.expectNoMsg(noMsgTimeout)
    client1.expectNoMsg(noMsgTimeout)
    expectNoMsg(noMsgTimeout)

    // GameStateUpdated не приходит вышедшим клиентам
    game ! UpdateGameState(newTime = 100)
    client0.expectNoMsg(noMsgTimeout)
    client1.expectNoMsg(noMsgTimeout)
    expectNoMsg(noMsgTimeout)
  })
}


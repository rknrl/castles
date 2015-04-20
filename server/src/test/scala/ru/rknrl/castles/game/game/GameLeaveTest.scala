//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.game

import akka.testkit.TestProbe
import ru.rknrl.castles.MatchMaking.PlayerLeaveGame
import ru.rknrl.castles.game.Game.Join
import ru.rknrl.castles.game.Game.UpdateGameState
import ru.rknrl.castles.matchmaking.NewMatchmaking.AllPlayersLeaveGame
import ru.rknrl.castles.rmi.B2C.{GameOver, JoinedGame}
import ru.rknrl.castles.rmi.C2B
import ru.rknrl.castles.rmi.C2B.Surrender
import ru.rknrl.dto.AccountType.{FACEBOOK, VKONTAKTE}
import ru.rknrl.dto.{AccountId, ItemType, PlayerId}

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

    client0.expectMsgPF(TIMEOUT) {
      case JoinedGame(gameStateDto) ⇒ true
    }

    client1.expectMsgPF(TIMEOUT) {
      case JoinedGame(gameStateDto) ⇒ true
    }

    // Если отправить LeaveGame раньше Surrender - они игнорируются

    client0.send(game, C2B.LeaveGame)
    client1.send(game, C2B.LeaveGame)
    client0.expectNoMsg()
    client1.expectNoMsg()
    expectNoMsg()

    // Игроки отправляют Surrender и получают в ответ GameOver

    client0.send(game, Surrender)

    client0.expectMsgPF(TIMEOUT) {
      case GameOver(dto) ⇒
        dto.playerId shouldBe PlayerId(0)
        dto.reward shouldBe 0
        dto.place shouldBe 2
    }

    client0.expectMsgPF(TIMEOUT) {
      case GameOver(dto) ⇒
        dto.playerId shouldBe PlayerId(1)
        dto.reward shouldBe 2
        dto.place shouldBe 1
    }

    client1.send(game, Surrender)

    client1.expectMsgPF(TIMEOUT) {
      case GameOver(dto) ⇒
        dto.playerId shouldBe PlayerId(0)
        dto.reward shouldBe 0
        dto.place shouldBe 2
    }

    client1.expectMsgPF(TIMEOUT) {
      case GameOver(dto) ⇒
        dto.playerId shouldBe PlayerId(1)
        dto.reward shouldBe 2
        dto.place shouldBe 1
    }

    // Если отправить LeaveGame с невалидного адреса - он игнорируется

    val client3 = new TestProbe(system)
    client3.send(game, C2B.LeaveGame)
    client0.expectNoMsg()
    client1.expectNoMsg()
    expectNoMsg()

    // Игроки отправляют LeaveGame
    // Матчмайкинг получает PlayerLeaveGame

    client0.send(game, C2B.LeaveGame)
    client0.expectNoMsg()
    expectMsgPF(TIMEOUT) {
      case PlayerLeaveGame(accountId, place, reward, usedItems, userInfo) ⇒
        accountId shouldBe AccountId(VKONTAKTE, "1")
        place shouldBe 2
        reward shouldBe 0
        usedItems shouldBe ItemType.values.map(_ → 0).toMap
        userInfo shouldBe initGameState.players(PlayerId(0)).userInfo
    }

    client1.send(game, C2B.LeaveGame)
    client1.expectNoMsg()
    expectMsgPF(TIMEOUT) {
      case PlayerLeaveGame(accountId, place, reward, usedItems, userInfo) ⇒
        accountId shouldBe AccountId(FACEBOOK, "1")
        place shouldBe 1
        reward shouldBe 2
        usedItems shouldBe ItemType.values.map(_ → 0).toMap
        userInfo shouldBe initGameState.players(PlayerId(1)).userInfo
    }

    // Матчмайкинг получает AllPlayersLeaveGame

    expectMsg(AllPlayersLeaveGame(game))

    // Если еще раз отправить LeaveGame они будут игнорироваться
    client0.send(game, C2B.LeaveGame)
    client1.send(game, C2B.LeaveGame)
    client0.expectNoMsg()
    client1.expectNoMsg()
    expectNoMsg()

    // GameStateUpdated не приходит вышедшим клиентам
    game ! UpdateGameState(newTime = 100)
    client0.expectNoMsg()
    client1.expectNoMsg()
    expectNoMsg()
  })
}


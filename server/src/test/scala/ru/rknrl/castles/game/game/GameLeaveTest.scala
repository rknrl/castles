//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.game

import akka.testkit.TestProbe
import ru.rknrl.castles.MatchMaking.{AllPlayersLeaveGame, PlayerLeaveGame}
import ru.rknrl.castles.game.Game.Join
import ru.rknrl.castles.rmi.B2C.{GameOver, JoinedGame}
import ru.rknrl.castles.rmi.C2B
import ru.rknrl.castles.rmi.C2B.Surrender
import ru.rknrl.dto.AccountType.{FACEBOOK, VKONTAKTE}
import ru.rknrl.dto.{AccountId, PlayerId}

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

    // Игроки отправляют LeaveGame
    // Матчмайкинг получает PlayerLeaveGame

    client0.send(game, C2B.LeaveGame)
    expectMsgClass(classOf[PlayerLeaveGame]) // todo check params

    client1.send(game, C2B.LeaveGame)
    expectMsgClass(classOf[PlayerLeaveGame]) // todo check params

    // Матчмайкинг получает AllPlayersLeaveGame

    expectMsg(AllPlayersLeaveGame)
  })
}

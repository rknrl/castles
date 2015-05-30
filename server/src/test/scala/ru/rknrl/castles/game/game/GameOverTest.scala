//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.game

import akka.testkit.TestProbe
import ru.rknrl.castles.game.Game.{Join, UpdateGameState}
import ru.rknrl.castles.game.state.GameItems
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.castles.rmi.B2C.{GameOver, GameStateUpdated, JoinedGame}
import ru.rknrl.core.points.Point
import ru.rknrl.dto.AccountType.{FACEBOOK, VKONTAKTE}
import ru.rknrl.dto.{AccountId, BuildingId, PlayerId}

class GameOverTest extends GameTestSpec {
  multi("GameOver", {

    // Создаем игру на двух игроков

    val game = newGame(gameState = gameStateMock(
      players = players,
      buildings = List(// <- всеми домиками на карте владеет player1
        buildingMock(id = BuildingId(0), pos = Point(0, 0), owner = Some(player1), count = 99),
        buildingMock(id = BuildingId(1), pos = Point(600, 600), owner = Some(player1), count = 99)
      ),
      items = new GameItems(players.map { case (id, player) ⇒ id → GameItems.init(player.items) }),
      assistancePositions = Map(
        PlayerId(0) → Point(20, 30),
        PlayerId(1) → Point(80, 90)
      )
    ))

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

    // После UpdateGameState оба игрока должны получить GameOver

    game ! UpdateGameState(newTime = 10)

    client0.expectMsgPF(TIMEOUT) {
      case GameStateUpdated(dto) ⇒ true
    }

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

    client1.expectMsgPF(TIMEOUT) {
      case GameStateUpdated(dto) ⇒ true
    }

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

    // Дальше геймоверы приходить не будут

    game ! UpdateGameState(newTime = 10)

    client0.expectMsgPF(TIMEOUT) {
      case GameStateUpdated(dto) ⇒ true
    }

    client0.expectNoMsg(noMsgTimeout)

    client1.expectMsgPF(TIMEOUT) {
      case GameStateUpdated(dto) ⇒ true
    }

    client1.expectNoMsg(noMsgTimeout)
  })
}

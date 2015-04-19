//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.game

import akka.testkit.TestProbe
import ru.rknrl.castles.game.Game.Join
import ru.rknrl.castles.game.NewGame.UpdateGameState
import ru.rknrl.castles.game.state.GameStateDiff
import ru.rknrl.castles.rmi.B2C.{GameStateUpdated, JoinedGame}
import ru.rknrl.dto.AccountType.{FACEBOOK, VKONTAKTE}
import ru.rknrl.dto.{AccountId, PlayerId}

class GameJoinTest extends GameTestSpec {
  multi("Join", {

    // Создаем игру на двух игроков

    val game = newGame(gameState = initGameState)
    val client0 = new TestProbe(system)
    val client1 = new TestProbe(system)

    // До Join'ов игра никому ничего не посылает

    game ! UpdateGameState(newTime = 7)

    client0.expectNoMsg()
    client1.expectNoMsg()

    // Игроки посылают Join

    client0.send(game, Join(AccountId(VKONTAKTE, "1"), client0.ref))
    client1.send(game, Join(AccountId(FACEBOOK, "1"), client1.ref))

    // Игроки получают в ответ JoinedGame с актуальным геймстейтом

    val newGameState = updateGameState(initGameState, newTime = 7)

    client0.expectMsgPF(TIMEOUT) {
      case JoinedGame(gameStateDto) ⇒ gameStateDto shouldBe newGameState.dto(
        id = PlayerId(0), // <- Проверяем, что playerId верный
        gameOvers = List.empty
      )
    }

    client1.expectMsgPF(TIMEOUT) {
      case JoinedGame(gameStateDto) ⇒ gameStateDto shouldBe newGameState.dto(
        id = PlayerId(1), // <- Проверяем, что playerId верный
        gameOvers = List.empty
      )
    }

    // Теперь после UpdateGameState оба игрока получают GameStateUpdated

    game ! UpdateGameState(newTime = 10)

    val gameStateUpdate = GameStateDiff.diff(newGameState, updateGameState(newGameState, newTime = 10))

    client0.expectMsgPF(TIMEOUT) {
      case GameStateUpdated(dto) ⇒ dto shouldBe gameStateUpdate
    }

    client1.expectMsgPF(TIMEOUT) {
      case GameStateUpdated(dto) ⇒ dto shouldBe gameStateUpdate
    }

    // Первый игрок переконнекчивается

    val newClient0 = new TestProbe(system)

    newClient0.send(game, Join(AccountId(VKONTAKTE, "1"), newClient0.ref))

    client0.expectNoMsg()

    newClient0.expectMsgPF(TIMEOUT) {
      case JoinedGame(gameStateDto) ⇒ gameStateDto.selfId shouldBe PlayerId(0)
    }

    // Теперь сообщения приходят на новый актор, а на старый не приходит

    game ! UpdateGameState(newTime = 20)

    val gameStateUpdate2 = GameStateDiff.diff(newGameState, updateGameState(newGameState, newTime = 20))

    client0.expectNoMsg()

    newClient0.expectMsgPF(TIMEOUT) {
      case GameStateUpdated(dto) ⇒ dto shouldBe gameStateUpdate2
    }

    client1.expectMsgPF(TIMEOUT) {
      case GameStateUpdated(dto) ⇒ dto shouldBe gameStateUpdate2
    }

  })
}

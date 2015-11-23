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
import protos.{AccountId, GameState, PlayerId}
import ru.rknrl.castles.game.Game.{Join, UpdateGameState}
import ru.rknrl.castles.game.state.GameStateDiff

class GameJoinTest extends GameTestSpec {
  multi("Join", {

    // Создаем игру на двух игроков

    val game = newGame(gameState = initGameState)
    val client0 = new TestProbe(system)
    val client1 = new TestProbe(system)

    // До Join'ов игра никому ничего не посылает

    game ! UpdateGameState(newTime = 7)

    client0.expectNoMsg(noMsgTimeout)
    client1.expectNoMsg(noMsgTimeout)

    // Игроки посылают Join

    client0.send(game, Join(AccountId(VKONTAKTE, "1"), client0.ref))
    client1.send(game, Join(AccountId(FACEBOOK, "1"), client1.ref))

    // Игроки получают в ответ актуальный геймстейт

    val newGameState = updateGameState(initGameState, newTime = 7)

    client0.expectMsg(newGameState.dto(
      id = PlayerId(0), // <- Проверяем, что playerId верный
      gameOvers = List.empty
    )
    )

    client1.expectMsg(newGameState.dto(
      id = PlayerId(1), // <- Проверяем, что playerId верный
      gameOvers = List.empty
    )
    )

    // Теперь после UpdateGameState оба игрока получают GameStateUpdated

    game ! UpdateGameState(newTime = 10)

    val gameStateUpdate = GameStateDiff.diff(newGameState, updateGameState(newGameState, newTime = 10))

    client0.expectMsg(gameStateUpdate)

    client1.expectMsg(gameStateUpdate)

    // Первый игрок переконнекчивается

    val newClient0 = new TestProbe(system)

    newClient0.send(game, Join(AccountId(VKONTAKTE, "1"), newClient0.ref))

    client0.expectNoMsg(noMsgTimeout)

    newClient0.expectMsgPF(TIMEOUT) {
      case gameStateDto: GameState ⇒ gameStateDto.selfId shouldBe PlayerId(0)
    }

    // Теперь сообщения приходят на новый актор, а на старый не приходит

    game ! UpdateGameState(newTime = 20)

    val gameStateUpdate2 = GameStateDiff.diff(newGameState, updateGameState(newGameState, newTime = 20))

    client0.expectNoMsg(noMsgTimeout)

    newClient0.expectMsg(gameStateUpdate2)

    client1.expectMsg(gameStateUpdate2)

  })
}

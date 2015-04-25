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
import ru.rknrl.castles.game.Game.UpdateGameState
import ru.rknrl.castles.game.state.{GameState, GameItems, GameStateDiff}
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.castles.rmi.B2C.{GameStateUpdated, JoinedGame}
import ru.rknrl.core.points.Point
import ru.rknrl.dto.AccountType.{FACEBOOK, VKONTAKTE}
import ru.rknrl.dto.{GameStateDTO, AccountId, BuildingId, PlayerId}

class GameSendToBotsTest extends GameTestSpec {
  multi("SendToBots", {

    // Создаем игру на игрока с ботом
    val player0 = playerMock(
      id = PlayerId(0),
      accountId = AccountId(VKONTAKTE, "1"),
      items = initItems
    )

    val player1 = playerMock(
      id = PlayerId(1),
      accountId = AccountId(FACEBOOK, "1"),
      items = initItems,
      isBot = true
    )

    val players = Map(
      PlayerId(0) → player0,
      PlayerId(1) → player1
    )

    def initGameState = gameStateMock(
      players = players,
      buildings = List(
        buildingMock(id = BuildingId(0), pos = Point(0, 0), owner = Some(player0), count = 99),
        buildingMock(id = BuildingId(1), pos = Point(600, 600), owner = Some(player1), count = 99)
      ),
      items = new GameItems(players.map { case (id, player) ⇒ id → GameItems.init(player.items) }),
      assistancePositions = Map(
        PlayerId(0) → Point(20, 30),
        PlayerId(1) → Point(80, 90)
      )
    )

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

    // И бот и игрок получают в ответ JoinedGame с актуальным геймстейтом

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

    // После UpdateGameState игрок получит GameStateUpdated, а бот - новый gameState

    game ! UpdateGameState(newTime = 10)

    val newGameState2 = updateGameState(newGameState, newTime = 10)
    val gameStateUpdate = GameStateDiff.diff(newGameState, newGameState2)

    client0.expectMsgPF(TIMEOUT) {
      case GameStateUpdated(dto) ⇒ true
    }

    client1.expectMsgPF(TIMEOUT) {
      case gameState: GameStateDTO ⇒ true
    }

  })
}
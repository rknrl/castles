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
import ru.rknrl.castles.matchmaking.MatchMaking.{AllPlayersLeaveGame, PlayerLeaveGame}
import ru.rknrl.castles.rmi.B2C.{GameOver, JoinedGame}
import ru.rknrl.castles.rmi.C2B
import ru.rknrl.castles.rmi.C2B.Surrender
import ru.rknrl.core.points.Point
import ru.rknrl.dto.AccountType.{FACEBOOK, VKONTAKTE}
import ru.rknrl.dto.{AccountId, BuildingId, ItemType, PlayerId}

class GameLeaveBotTest extends GameTestSpec {
  multi("Leave", {
    val player0 = playerMock(
      id = PlayerId(0),
      accountId = AccountId(VKONTAKTE, "1"),
      items = initItems
    )

    val player1 = playerMock(
      id = PlayerId(1),
      accountId = AccountId(FACEBOOK, "1"),
      items = initItems,
      isBot = true // <- бот
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

    // Создаем игру на игрока и бота

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

    client0.expectMsgClass(classOf[GameOver])
    client0.expectMsgClass(classOf[GameOver])

    client1.expectMsgClass(classOf[GameOver])
    client1.expectMsgClass(classOf[GameOver])

    // Игроки отправляют LeaveGame
    // Матчмайкинг получает PlayerLeaveGame про живого игрока, но не получает про бота

    client0.send(game, C2B.LeaveGame)
    client0.expectNoMsg()

    expectMsgPF(TIMEOUT) {
      case PlayerLeaveGame(accountId, place, reward, usedItems) ⇒
        accountId shouldBe AccountId(VKONTAKTE, "1")
        place shouldBe 2
        reward shouldBe 0
        usedItems shouldBe ItemType.values.map(_ → 0).toMap
    }

    client1.send(game, C2B.LeaveGame)
    client1.expectNoMsg()

    // Матчмайкинг получает AllPlayersLeaveGame

    expectMsg(AllPlayersLeaveGame(game))
  })
}


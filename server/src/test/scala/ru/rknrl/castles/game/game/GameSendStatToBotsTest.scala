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
import ru.rknrl.castles.game.Game.Join
import ru.rknrl.castles.game.state.{GameItems, GameState}
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.core.points.Point

class GameSendStatToBotsTest extends GameTestSpec {
  multi("SendStatToBots", {

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

    // Игроки посылают Join

    client0.send(game, Join(AccountId(VKONTAKTE, "1"), client0.ref))
    client1.send(game, Join(AccountId(FACEBOOK, "1"), client1.ref))

    // И бот и игрок получают в ответ JoinedGame с актуальным геймстейтом

    val newGameState = updateGameState(initGameState, newTime = 7)

    client0.expectMsgClass(classOf[protos.GameState])
    client1.expectMsgClass(classOf[protos.GameState])

    // StatAction пересылается всем ботам

    game ! StatAction.TUTOR_BIG_TOWER

    client0.expectNoMsg(noMsgTimeout)

    client1.expectMsg(StatAction.TUTOR_BIG_TOWER)

  })
}

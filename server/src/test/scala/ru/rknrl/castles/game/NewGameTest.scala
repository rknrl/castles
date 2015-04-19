//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game

import akka.actor.{ActorRef, Props}
import org.scalatest.{Matchers, WordSpecLike}
import ru.rknrl.castles.game.Game.{Join, UpdateGameState}
import ru.rknrl.castles.game.state.{GameItems, GameState}
import ru.rknrl.castles.kit.ActorsTest
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.castles.rmi.B2C.JoinedGame
import ru.rknrl.castles.rmi.C2B.GameMsg
import ru.rknrl.dto.AccountType.{FACEBOOK, VKONTAKTE}
import ru.rknrl.dto.{AccountId, PlayerId}

class NewGameTest extends ActorsTest with WordSpecLike with Matchers {

  var gameIterator = 0

  def newGame(gameState: GameState = gameStateMock(),
              config: GameConfig = gameConfigMock(),
              matchmaking: ActorRef = self,
              bugs: ActorRef = self) = {
    gameIterator += 1
    system.actorOf(Props(classOf[NewGame], gameState, config, matchmaking, bugs), "game" + gameIterator)
  }

  multi("Join", {
    val players = Map(
      PlayerId(0) → playerMock(
        id = PlayerId(0),
        accountId = AccountId(VKONTAKTE, "1")
      ),
      PlayerId(1) → playerMock(
        id = PlayerId(1),
        accountId = AccountId(FACEBOOK, "1")
      )
    )

    val gameState = gameStateMock(
      players = players,
      items = new GameItems(players.map { case (id, player) ⇒ id → GameItems.init(player.items) })
    )

    val game = newGame(gameState = gameState)
    game ! Join(AccountId(VKONTAKTE, "1"), self)
    expectMsgPF(timeout.duration) {
      case JoinedGame(gameStateDto) ⇒ gameStateDto shouldBe gameState.dto(
        id = PlayerId(0), // <- Верно определил playerId
        gameOvers = List.empty
      )
    }

    game ! Join(AccountId(FACEBOOK, "1"), self)
    expectMsgPF(timeout.duration) {
      case JoinedGame(gameStateDto) ⇒ gameStateDto shouldBe gameState.dto(
        id = PlayerId(1), // <- Верно определил playerId
        gameOvers = List.empty
      )
    }
  })

  multi("Join & UpdateGameState", {
    val players = Map(
      PlayerId(0) → playerMock(
        id = PlayerId(0),
        accountId = AccountId(VKONTAKTE, "1")
      ),
      PlayerId(1) → playerMock(
        id = PlayerId(1),
        accountId = AccountId(FACEBOOK, "1")
      )
    )

    val gameState = gameStateMock(
      players = players,
      buildings = List(buildingMock()),
      items = new GameItems(players.map { case (id, player) ⇒ id → GameItems.init(player.items) })
    )

    val game = newGame(gameState = gameState)

    game ! UpdateGameState

    expectNoMsg()

    game ! Join(AccountId(VKONTAKTE, "1"), self)
    expectMsgPF(timeout.duration) {
      case JoinedGame(gameStateDto) ⇒ gameStateDto shouldBe gameState.dto(
        id = PlayerId(0),
        gameOvers = List.empty
      )
    }

    game ! UpdateGameState

    expectMsgPF(timeout.duration) {
      case _: GameMsg ⇒ true
    }
  })

}

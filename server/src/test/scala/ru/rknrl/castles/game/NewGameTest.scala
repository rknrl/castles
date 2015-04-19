//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game

import akka.actor.{ActorRef, Props}
import akka.testkit.TestProbe
import org.scalatest.{Matchers, WordSpecLike}
import ru.rknrl.castles.account.AccountState.Items
import ru.rknrl.castles.game.Game.Join
import ru.rknrl.castles.game.NewGame.UpdateGameState
import ru.rknrl.castles.game.state.{GameItems, GameState, GameStateDiff}
import ru.rknrl.castles.kit.ActorsTest
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.castles.rmi.B2C.{GameStateUpdated, JoinedGame}
import ru.rknrl.castles.rmi.C2B._
import ru.rknrl.core.points.Point
import ru.rknrl.dto.AccountType.{FACEBOOK, VKONTAKTE}
import ru.rknrl.dto.ItemType._
import ru.rknrl.dto._

import scala.concurrent.duration._

class NewGameTest extends ActorsTest with WordSpecLike with Matchers {

  val TIMEOUT = 100 millis

  var gameIterator = 0

  def newGame(gameState: GameState = gameStateMock(),
              config: GameConfig = gameConfigMock(),
              matchmaking: ActorRef = self,
              bugs: ActorRef = self) = {
    gameIterator += 1
    system.actorOf(Props(classOf[NewGame], gameState, config, matchmaking, bugs), "game" + gameIterator)
  }

  def updateGameState(gameState: GameState,
                      newTime: Long = 1,
                      moveActions: Map[PlayerId, MoveDTO] = Map.empty,
                      fireballCasts: Map[PlayerId, PointDTO] = Map.empty,
                      volcanoCasts: Map[PlayerId, PointDTO] = Map.empty,
                      tornadoCasts: Map[PlayerId, CastTornadoDTO] = Map.empty,
                      strengtheningCasts: Map[PlayerId, BuildingId] = Map.empty,
                      assistanceCasts: Map[PlayerId, BuildingId] = Map.empty) =
    gameState.update(
      newTime = newTime,
      moveActions = moveActions,
      fireballCasts = fireballCasts,
      volcanoCasts = volcanoCasts,
      tornadoCasts = tornadoCasts,
      strengtheningCasts = strengtheningCasts,
      assistanceCasts = assistanceCasts
    )

  def initItems: Items = Map(
    FIREBALL → 4,
    TORNADO → 4,
    VOLCANO → 4,
    STRENGTHENING → 4,
    ASSISTANCE → 4
  )

  val player0 = playerMock(
    id = PlayerId(0),
    accountId = AccountId(VKONTAKTE, "1"),
    items = initItems
  )

  val player1 = playerMock(
    id = PlayerId(1),
    accountId = AccountId(FACEBOOK, "1"),
    items = initItems
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
  })

  multi("Move", {
    val move0 = MoveDTO(fromBuildings = List(BuildingId(0)), toBuilding = BuildingId(1))
    val move1 = MoveDTO(fromBuildings = List(BuildingId(1)), toBuilding = BuildingId(0))

    checkCasts(
      cast0 = Move(move0),
      cast1 = Move(move1),
      newGameState = updateGameState(
        initGameState,
        newTime = 10,
        moveActions = Map(
          PlayerId(0) → move0,
          PlayerId(1) → move1
        )
      )
    )
  })


  multi("CastFireball", {
    val fireball0 = PointDTO(600, 600)
    val fireball1 = PointDTO(0, 0)

    checkCasts(
      cast0 = CastFireball(fireball0),
      cast1 = CastFireball(fireball1),
      newGameState = updateGameState(
        initGameState,
        newTime = 10,
        fireballCasts = Map(
          PlayerId(0) → fireball0,
          PlayerId(1) → fireball1
        )
      )
    )
  })

  multi("CastVolcano", {
    val volcano0 = PointDTO(600, 600)
    val volcano1 = PointDTO(0, 0)

    checkCasts(
      cast0 = CastVolcano(volcano0),
      cast1 = CastVolcano(volcano1),
      newGameState = updateGameState(
        initGameState,
        newTime = 10,
        volcanoCasts = Map(
          PlayerId(0) → volcano0,
          PlayerId(1) → volcano1
        )
      )
    )
  })

  multi("CastTornado", {
    val tornado0 = CastTornadoDTO(List(PointDTO(600, 600), PointDTO(601, 601), PointDTO(602, 602)))
    val tornado1 = CastTornadoDTO(List(PointDTO(0, 0), PointDTO(1, 1), PointDTO(2, 2)))

    checkCasts(
      cast0 = CastTornado(tornado0),
      cast1 = CastTornado(tornado1),
      newGameState = updateGameState(
        initGameState,
        newTime = 10,
        tornadoCasts = Map(
          PlayerId(0) → tornado0,
          PlayerId(1) → tornado1
        )
      )
    )
  })

  multi("CastStrengthening", {
    val strengthening0 = BuildingId(0)
    val strengthening1 = BuildingId(1)

    checkCasts(
      cast0 = CastStrengthening(strengthening0),
      cast1 = CastStrengthening(strengthening1),
      newGameState = updateGameState(
        initGameState,
        newTime = 10,
        strengtheningCasts = Map(
          PlayerId(0) → strengthening0,
          PlayerId(1) → strengthening1
        )
      )
    )
  })

  multi("CastAssistance", {
    val strengthening0 = BuildingId(0)
    val strengthening1 = BuildingId(1)

    checkCasts(
      cast0 = CastAssistance(strengthening0),
      cast1 = CastAssistance(strengthening1),
      newGameState = updateGameState(
        initGameState,
        newTime = 10,
        assistanceCasts = Map(
          PlayerId(0) → strengthening0,
          PlayerId(1) → strengthening1
        )
      )
    )
  })

  def checkCasts(cast0: Any, cast1: Any, newGameState: GameState) = {
    // Создаем игру на двух игроков

    val game = newGame(gameState = initGameState)
    val client0 = new TestProbe(system)
    val client1 = new TestProbe(system)

    // Игроки посылают Join

    client0.send(game, Join(AccountId(VKONTAKTE, "1"), client0.ref))
    client1.send(game, Join(AccountId(FACEBOOK, "1"), client1.ref))

    // Игроки получают в ответ JoinedGame

    client0.expectMsgPF(TIMEOUT) {
      case JoinedGame(gameStateDto) ⇒ true
    }

    client1.expectMsgPF(TIMEOUT) {
      case JoinedGame(gameStateDto) ⇒ true
    }

    // Игроки делают касты

    client0.send(game, cast0)
    client1.send(game, cast1)

    // Оба игрока получают GameStateUpdated с учетом этих кастов

    game ! UpdateGameState(newTime = 10)

    val gameStateUpdate = GameStateDiff.diff(initGameState, newGameState)

    client0.expectMsgPF(TIMEOUT) {
      case GameStateUpdated(dto) ⇒ dto shouldBe gameStateUpdate
    }

    client1.expectMsgPF(TIMEOUT) {
      case GameStateUpdated(dto) ⇒ dto shouldBe gameStateUpdate
    }

    // В следующий раз касты не учитываются

    game ! UpdateGameState(newTime = 20)

    val gameStateUpdate2 = GameStateDiff.diff(
      newGameState,

      updateGameState(
        newGameState,
        newTime = 20
      )
    )

    client0.expectMsgPF(TIMEOUT) {
      case GameStateUpdated(dto) ⇒ dto shouldBe gameStateUpdate2
    }

    client1.expectMsgPF(TIMEOUT) {
      case GameStateUpdated(dto) ⇒ dto shouldBe gameStateUpdate2
    }
  }

}

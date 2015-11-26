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
import ru.rknrl.castles.game.Game.{Join, UpdateGameState}
import ru.rknrl.castles.game.state.{GameState, GameStateDiff}

class GameCastsTest extends GameTestSpec {

  multi("Move", {
    val move0 = protos.Move(fromBuildings = List(BuildingId(0)), toBuilding = BuildingId(1))
    val move1 = protos.Move(fromBuildings = List(BuildingId(1)), toBuilding = BuildingId(0))

    checkCasts(
      cast0 = move0,
      cast1 = move1,
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
    val tornado0 = CastTornado(List(PointDTO(600, 600), PointDTO(601, 601), PointDTO(602, 602)))
    val tornado1 = CastTornado(List(PointDTO(0, 0), PointDTO(1, 1), PointDTO(2, 2)))

    checkCasts(
      cast0 = tornado0,
      cast1 = tornado1,
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

    client0.expectMsgClass(classOf[GameState])

    client1.expectMsgClass(classOf[GameState])

    // Игроки делают касты

    client0.send(game, cast0)
    client1.send(game, cast1)

    // Оба игрока получают GameStateUpdated с учетом этих кастов

    game ! UpdateGameState(newTime = 10)

    val gameStateUpdate = GameStateDiff.diff(initGameState, newGameState)

    client0.expectMsg(gameStateUpdate)

    client1.expectMsg(gameStateUpdate)

    // В следующий раз касты не учитываются

    game ! UpdateGameState(newTime = 20)

    val newGameState2 = updateGameState(
      newGameState,
      newTime = 20
    )

    val gameStateUpdate2 = GameStateDiff.diff(
      newGameState,
      newGameState2
    )

    client0.expectMsg(gameStateUpdate2)

    client1.expectMsg(gameStateUpdate2)

    // -------------------------------------------------------
    // Если отправить каст с невалидного адреса - он игнорируется

    val client3 = new TestProbe(system)
    client3.send(game, cast0)

    game ! UpdateGameState(newTime = 50)

    val newGameState3 = updateGameState(newGameState2, newTime = 50)
    val gameStateUpdate3 = GameStateDiff.diff(newGameState2, newGameState3)

    client0.expectMsg(gameStateUpdate3)

    client1.expectMsg(gameStateUpdate3)

    // -------------------------------------------------------
    // Если игрок проиграл или сдался - его касты игнорируются

    client0.send(game, Surrender)

    // Оба игрока получают GameOver в ответ

    client0.expectMsgClass(classOf[GameOver])
    client0.expectMsgClass(classOf[GameOver])
    client1.expectMsgClass(classOf[GameOver])
    client1.expectMsgClass(classOf[GameOver])

    // Игроки делают касты

    client0.send(game, cast0)
    client1.send(game, cast1)

    // Касты игнорируются

    game ! UpdateGameState(newTime = 100)

    val gameStateUpdate4 = GameStateDiff.diff(
      newGameState3,
      updateGameState(newGameState3, newTime = 100)
    )

    client0.expectMsg(gameStateUpdate4)

    client1.expectMsg(gameStateUpdate4)

  }
}

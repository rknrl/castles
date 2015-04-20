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
import ru.rknrl.castles.game.state.GameItems
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.castles.rmi.B2C.{GameOver, GameStateUpdated, JoinedGame}
import ru.rknrl.castles.rmi.C2B
import ru.rknrl.castles.rmi.C2B.Surrender
import ru.rknrl.core.points.Point
import ru.rknrl.dto.AccountType.{FACEBOOK, VKONTAKTE}
import ru.rknrl.dto.{AccountId, BuildingId, PlayerId}

class GameSurrenderTest extends GameTestSpec {
   multi("Surrender", {

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

     // Если отправить Surrender с невалидного адреса - он игнорируется

     val client3 = new TestProbe(system)
     client3.send(game, Surrender)
     client0.expectNoMsg()
     client1.expectNoMsg()
     expectNoMsg()

     // Первый игрок посылает Surrender

     client0.send(game, Surrender)

     // Оба игрока получают GameOver в ответ

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

     client0.expectNoMsg()

     client1.expectMsgPF(TIMEOUT) {
       case GameStateUpdated(dto) ⇒ true
     }

     client1.expectNoMsg()

     // Если еще раз отправить Surrender - они будут игнорироваться

     client0.send(game, Surrender)
     client1.send(game, Surrender)
     client0.expectNoMsg()
     client1.expectNoMsg()
   })
 }

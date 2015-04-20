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
import ru.rknrl.castles.rmi.B2C.{GameOver, GameStateUpdated, JoinedGame}
import ru.rknrl.castles.rmi.C2B.Surrender
import ru.rknrl.dto.AccountType.{FACEBOOK, VKONTAKTE}
import ru.rknrl.dto.{AccountId, PlayerId}

class GameSurrenderRejectTest extends GameTestSpec {
   multi("Surrender", {

     // Создаем игру на двух игроков, isDev = false

     val game = newGame(gameState = initGameState, isDev = false)

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

     // Если отправить Surrender - он игнорируется

     client0.send(game, Surrender)
     client0.expectNoMsg()
     client1.expectNoMsg()
     expectNoMsg()
   })
 }

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
import protos.{AccountId, GameState, Surrender}
import ru.rknrl.castles.game.Game.Join

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

    client0.expectMsgClass(classOf[GameState])

    client1.expectMsgClass(classOf[GameState])

    // Если отправить Surrender - он игнорируется

    client0.send(game, Surrender())
    client0.expectNoMsg(noMsgTimeout)
    client1.expectNoMsg(noMsgTimeout)
    expectNoMsg(noMsgTimeout)
  })
}

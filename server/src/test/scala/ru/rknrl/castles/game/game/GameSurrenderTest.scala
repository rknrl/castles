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

    client0.expectMsgClass(classOf[GameState])

    client1.expectMsgClass(classOf[GameState])

    // Если отправить Surrender с невалидного адреса - он игнорируется

    val client3 = new TestProbe(system)
    client3.send(game, Surrender())
    client0.expectNoMsg(noMsgTimeout)
    client1.expectNoMsg(noMsgTimeout)
    expectNoMsg(noMsgTimeout)

    // Первый игрок посылает Surrender

    client0.send(game, Surrender())

    // Оба игрока получают GameOver в ответ

    client0.expectMsgPF(TIMEOUT) {
      case dto: GameOver ⇒
        dto.playerId shouldBe PlayerId(0)
        dto.reward shouldBe 0
        dto.place shouldBe 2
    }

    client0.expectMsgPF(TIMEOUT) {
      case dto: GameOver ⇒
        dto.playerId shouldBe PlayerId(1)
        dto.reward shouldBe 2
        dto.place shouldBe 1
    }

    client1.expectMsgPF(TIMEOUT) {
      case dto: GameOver ⇒
        dto.playerId shouldBe PlayerId(0)
        dto.reward shouldBe 0
        dto.place shouldBe 2
    }

    client1.expectMsgPF(TIMEOUT) {
      case dto: GameOver ⇒
        dto.playerId shouldBe PlayerId(1)
        dto.reward shouldBe 2
        dto.place shouldBe 1
    }

    // Дальше геймоверы приходить не будут

    game ! UpdateGameState(newTime = 10)

    client0.expectMsgClass(classOf[GameStateUpdate])

    client0.expectNoMsg(noMsgTimeout)

    client1.expectMsgClass(classOf[GameStateUpdate])

    client1.expectNoMsg(noMsgTimeout)

    // Если еще раз отправить Surrender - они будут игнорироваться

    client0.send(game, Surrender())
    client1.send(game, Surrender())
    client0.expectNoMsg(noMsgTimeout)
    client1.expectNoMsg(noMsgTimeout)
  })
}

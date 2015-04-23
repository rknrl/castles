//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.game

import akka.testkit.TestProbe
import ru.rknrl.castles.game.FakeBotFactory
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.castles.matchmaking.MatchMaking.ConnectToGame
import ru.rknrl.dto.AccountType.{FACEBOOK, VKONTAKTE}
import ru.rknrl.dto.{AccountId, PlayerId}

class GameCreateBotsTest extends GameTestSpec {

  "create bots" in {
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

    val bot = new TestProbe(system)
    val game = newGame(
      gameState = gameStateMock(players = players),
      botFactory = new FakeBotFactory(bot.ref)
    )
    bot.expectMsg(ConnectToGame(game))
  }

}

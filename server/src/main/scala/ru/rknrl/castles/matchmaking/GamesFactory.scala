//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.matchmaking

import akka.actor.{ActorContext, ActorRef}
import ru.rknrl.castles.game.GameScheduler
import ru.rknrl.castles.kit.Mocks
import ru.rknrl.castles.matchmaking.NewMatchmaking.{GameInfoNew, GameOrderNew}
import ru.rknrl.dto.AccountId

class GamesFactory {
  def createGames(accountIdToGameOrder: Map[AccountId, GameOrderNew],
                  matchmaking: ActorRef,
                  gameFactory: IGameFactory)
                 (implicit context: ActorContext) =
    for ((accountId, order) ← accountIdToGameOrder) yield
    accountId → GameInfoNew(ref = gameFactory.create(
      gameState = Mocks.gameStateMock(),
      isDev = true,
      schedulerClass = classOf[GameScheduler],
      matchmaking = matchmaking,
      bugs = matchmaking
    ))

}


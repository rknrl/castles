//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.matchmaking

import akka.actor.{ActorContext, ActorRef, Props}
import ru.rknrl.castles.game.state.GameState
import ru.rknrl.castles.game.{Game, GameScheduler}
import ru.rknrl.castles.kit.Mocks
import ru.rknrl.castles.matchmaking.NewMatchmaking.{GameInfo, GameOrder}
import ru.rknrl.dto.AccountId

trait IGamesFactory {
  def createGames(accountIdToGameOrder: Map[AccountId, GameOrder],
                  matchmaking: ActorRef)
                 (implicit context: ActorContext): Map[AccountId, GameInfo]
}

class GamesFactory(gameFactory: IGameFactory) extends IGamesFactory {

  def createGames(accountIdToGameOrder: Map[AccountId, GameOrder],
                  matchmaking: ActorRef)
                 (implicit context: ActorContext) =
    for ((accountId, order) ← accountIdToGameOrder) yield
    accountId → GameInfo(
      gameRef = gameFactory.create(
        gameState = Mocks.gameStateMock(),
        isDev = true,
        schedulerClass = classOf[GameScheduler],
        matchmaking = matchmaking,
        bugs = matchmaking
      ),
      orders = List(order),
      isTutor = false
    )

}

//

trait IGameFactory {
  def create(gameState: GameState,
             isDev: Boolean,
             schedulerClass: Class[_],
             matchmaking: ActorRef,
             bugs: ActorRef)(implicit context: ActorContext): ActorRef
}

class GameFactory extends IGameFactory {
  private var gameIterator = 0L

  def create(gameState: GameState,
             isDev: Boolean,
             schedulerClass: Class[_],
             matchmaking: ActorRef,
             bugs: ActorRef)(implicit context: ActorContext) = {
    val game = context.actorOf(Props(classOf[Game], gameState, isDev, schedulerClass, matchmaking, bugs), "game-" + gameIterator)
    gameIterator += 1
    game
  }
}

class FakeGameFactory(ref: ActorRef) extends IGameFactory {
  def create(gameState: GameState,
             isDev: Boolean,
             schedulerClass: Class[_],
             matchmaking: ActorRef,
             bugs: ActorRef)(implicit context: ActorContext) = ref
}



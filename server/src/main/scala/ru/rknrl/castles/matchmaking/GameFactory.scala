//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.matchmaking

import akka.actor.{ActorContext, ActorRef}
import ru.rknrl.castles.game.state.GameState
import ru.rknrl.castles.game.{BotFactory, Game, GameScheduler}

trait IGameFactory {
  def create(gameState: GameState,
             isDev: Boolean,
             isTutor: Boolean,
             matchmaking: ActorRef)
            (implicit context: ActorContext): ActorRef
}

class GameFactory extends IGameFactory {
  private var gameIterator = 0L

  def create(gameState: GameState,
             isDev: Boolean,
             isTutor: Boolean,
             matchmaking: ActorRef)(implicit context: ActorContext) = {
    val game = context.actorOf(Game.props(gameState, isDev, isTutor, new BotFactory(), classOf[GameScheduler], matchmaking), "game-" + gameIterator)
    gameIterator += 1
    game
  }
}

class FakeGameFactory(ref: ActorRef) extends IGameFactory {
  def create(gameState: GameState,
             isDev: Boolean,
             isTutor: Boolean,
             matchmaking: ActorRef)(implicit context: ActorContext) = ref
}



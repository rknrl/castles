//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.matchmaking

import akka.actor.{ActorContext, ActorRef, Props}
import ru.rknrl.castles.game.Game
import ru.rknrl.castles.game.state.GameState

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

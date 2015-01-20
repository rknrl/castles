package ru.rknrl.castles.bot

import akka.actor.{Actor, ActorRef}
import ru.rknrl.base.game.Game
import ru.rknrl.castles.AccountId
import ru.rknrl.castles.MatchMaking.ConnectToGame
import Game.Join
import ru.rknrl.castles.game.GameState
import ru.rknrl.castles.rmi.JoinGameMsg
import ru.rknrl.dto.GameDTO.GameStateDTO

class Bot(val externalAccountId: AccountId) extends Actor {

  private var game: Option[ActorRef] = None
  private var gameState: Option[GameState] = None

  override def receive: Receive = {
    case ConnectToGame(gameRef) ⇒
      game = Some(gameRef)
      gameRef ! Join(externalAccountId, self, self)

    case JoinGameMsg(gameState: GameStateDTO) ⇒

    case newGameState: GameState ⇒
      gameState = Some(newGameState)

    case _ ⇒
    //    MoveMsg()
    //    CastFireballMsg(),
    //    CastStrengtheningMsg(),
    //    CastVolcanoMsg(),
    //    CastTornadoMsg(),
    //    CastAssistanceMsg(),
    //    LeaveMsg()
  }
}

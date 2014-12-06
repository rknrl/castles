package ru.rknrl.castles.bot

import akka.actor.{Actor, ActorRef}
import ru.rknrl.castles.AccountId
import ru.rknrl.castles.MatchMaking.ConnectToGame
import ru.rknrl.castles.game.Game.Join
import ru.rknrl.castles.game.GameState
import ru.rknrl.castles.rmi.b2c._
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

//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.bot

import akka.actor.ActorLogging
import ru.rknrl.castles.AccountId
import ru.rknrl.castles.game.GameConfig
import ru.rknrl.castles.game.state.GameState
import ru.rknrl.castles.rmi.C2B.StartTutorGame

class BotTutor(accountId: AccountId, config: GameConfig) extends Bot(accountId, config) with ActorLogging {

  var active: Boolean = false

  def tutorBotReceive: Receive = {
    case StartTutorGame ⇒
      active = true
  }

  override def receive = tutorBotReceive.orElse(super.receive)

  override def update(newGameState: GameState) = {
    if (active) super.update(newGameState)
  }
}

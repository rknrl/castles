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

class BotTutor(accountId: AccountId, config: GameConfig) extends Bot(accountId, config) with ActorLogging {
  override def update(newGameState: GameState) = {

  }
}

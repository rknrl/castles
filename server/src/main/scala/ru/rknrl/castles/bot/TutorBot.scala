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
import ru.rknrl.castles.rmi.C2B._
import ru.rknrl.dto.CommonDTO.{BuildingLevel, BuildingType}
import ru.rknrl.dto.GameDTO.MoveDTO

import scala.collection.JavaConverters._

object BotMode extends Enumeration {
  type BotMode = Value

  val NONE = Value
  val SEND_UNITS_TO_ONE_BUILDING = Value
  val GAME = Value
}

import ru.rknrl.castles.bot.BotMode._

class TutorBot(accountId: AccountId, config: GameConfig) extends Bot(accountId, config) with ActorLogging {

  var mode = NONE

  def tutorBotReceive: Receive = {
    case StartTutorGame ⇒
      mode = GAME

    case StartTutorFireball ⇒
      if (playerId.get.id == 3) mode = SEND_UNITS_TO_ONE_BUILDING
  }

  override def receive = tutorBotReceive.orElse(super.receive)

  override def update(newGameState: GameState) = {
    gameState = Some(newGameState)
    mode match {
      case SEND_UNITS_TO_ONE_BUILDING ⇒
        val time = newGameState.time

        if (time - lastTime > interval) {
          lastTime = time

          sender ! Move(
            MoveDTO.newBuilder()
              .addAllFromBuildings(getMyBuildings.map(_.id.dto).asJava)
              .setToBuilding(towers.last.id.dto)
              .build()
          )
        }
      case GAME ⇒
        super.update(newGameState)
      case NONE ⇒
    }

    def towers = buildings
      .filter(b ⇒ b.prototype.buildingType == BuildingType.TOWER &&
      b.prototype.level == BuildingLevel.LEVEL_2)
  }
}
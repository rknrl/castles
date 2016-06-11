//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.bot

import protos._

object BotMode extends Enumeration {
  type BotMode = Value

  val NONE = Value
  val SEND_UNITS_TO_ONE_BUILDING = Value
  val GAME = Value
}

import ru.rknrl.castles.bot.BotMode._

class TutorBot(accountId: AccountId) extends GameBot(accountId) {

  var mode = NONE

  override def receive = logged(baseReceive orElse {
    case StatAction.TUTOR_WIN_CHALLENGE ⇒
      mode = GAME

    case StatAction.TUTOR_ARROWS ⇒
      if (playerId.get.id == 3) mode = SEND_UNITS_TO_ONE_BUILDING

  })

  override def update(newGameState: GameState) = {
    gameState = Some(newGameState)
    mode match {
      case SEND_UNITS_TO_ONE_BUILDING ⇒
        val time = System.currentTimeMillis()

        if (time - lastTime > moveInterval) {
          lastTime = time

          send(sender,
            Move(
              getMyBuildings.map(_.id),
              towers.last.id
            )
          )
        }
      case GAME ⇒
        super.update(newGameState)
      case NONE ⇒
    }

    def towers = buildings
      .filter(b ⇒ b.building.buildingType == BuildingType.TOWER &&
        b.building.buildingLevel == BuildingLevel.LEVEL_2)
  }
}
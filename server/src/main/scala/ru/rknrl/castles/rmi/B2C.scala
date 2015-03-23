//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.rmi

import ru.rknrl.core.rmi.Msg
import ru.rknrl.dto.AccountDTO.AccountStateDTO
import ru.rknrl.dto.AdminDTO.{AdminAccountStateDTO, AdminOnlineDTO}
import ru.rknrl.dto.AuthDTO.AuthenticatedDTO
import ru.rknrl.dto.CommonDTO.NodeLocator
import ru.rknrl.dto.GameDTO._

object B2C {

  // auth

  case class Authenticated(success: AuthenticatedDTO) extends Msg(1)

  // admin

  case object AuthenticatedAsAdmin extends Msg(2)

  case class AdminOnline(online: AdminOnlineDTO) extends Msg(3)

  // account

  case class AccountStateUpdated(accountState: AccountStateDTO) extends Msg(4)

  case class EnteredGame(node: NodeLocator) extends Msg(5)

  case class AccountState(adminAccountState: AdminAccountStateDTO) extends Msg(6)

  // enter game

  case class JoinedGame(gameState: GameStateDTO) extends Msg(7)

  case object LeavedGame extends Msg(8)

  // game

  case class UpdateBuilding(building: BuildingUpdateDTO) extends Msg(9)

  case class UpdateItemStates(states: ItemsStateDTO) extends Msg(10)

  case class AddUnit(unit: UnitDTO) extends Msg(11)

  case class UpdateUnit(unitUpdate: UnitUpdateDTO) extends Msg(12)

  case class RemoveUnit(id: UnitIdDTO) extends Msg(13)

  case class KillUnit(killedId: UnitIdDTO) extends Msg(14)

  case class AddFireball(fireball: FireballDTO) extends Msg(15)

  case class AddVolcano(volcano: VolcanoDTO) extends Msg(16)

  case class AddTornado(tornado: TornadoDTO) extends Msg(17)

  case class AddBullet(bullet: BulletDTO) extends Msg(18)

  case class GameOver(gameOver: GameOverDTO) extends Msg(19)

}
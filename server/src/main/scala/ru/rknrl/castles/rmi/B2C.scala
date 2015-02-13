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
import ru.rknrl.dto.AdminDTO.{AdminOnlineDTO, AdminAccountStateDTO}
import ru.rknrl.dto.AuthDTO.AuthenticatedDTO
import ru.rknrl.dto.CommonDTO.NodeLocator
import ru.rknrl.dto.GameDTO._

object B2C {

  // auth

  case class Authenticated(success: AuthenticatedDTO) extends Msg

  // admin

  case object AuthenticatedAsAdmin extends Msg

  case class AdminOnline(online: AdminOnlineDTO) extends Msg

  // account

  case class AccountStateUpdated(accountState: AccountStateDTO) extends Msg

  case class EnteredGame(node: NodeLocator) extends Msg

  // enter game

  case class JoinedGame(gameState: GameStateDTO) extends Msg

  case object LeavedGame extends Msg

  // game

  case class UpdateBuilding(building: BuildingUpdateDTO) extends Msg

  case class UpdateItemStates(states: ItemsStateDTO) extends Msg

  case class AddUnit(unit: UnitDTO) extends Msg

  case class UpdateUnit(unitUpdate: UnitUpdateDTO) extends Msg

  case class RemoveUnit(id: UnitIdDTO) extends Msg

  case class AddFireball(fireball: FireballDTO) extends Msg

  case class AddVolcano(volcano: VolcanoDTO) extends Msg

  case class AddTornado(tornado: TornadoDTO) extends Msg

  case class AddBullet(bullet: BulletDTO) extends Msg

  case class GameOver(gameOver: GameOverDTO) extends Msg

  // admin

  case class AccountState(adminAccountState: AdminAccountStateDTO) extends Msg

}
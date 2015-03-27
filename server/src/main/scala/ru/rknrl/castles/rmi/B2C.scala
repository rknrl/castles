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
import ru.rknrl.dto.AuthDTO.{TopDTO, AuthenticatedDTO}
import ru.rknrl.dto.CommonDTO.NodeLocator
import ru.rknrl.dto.GameDTO._

object B2C {

  // auth 0 - 9

  case class Authenticated(success: AuthenticatedDTO) extends Msg(1)

  // admin 10 - 19

  case object AuthenticatedAsAdmin extends Msg(10)

  case class AdminOnline(online: AdminOnlineDTO) extends Msg(11)

  // account 20 - 29

  case class AccountStateUpdated(accountState: AccountStateDTO) extends Msg(20)

  case class EnteredGame(node: NodeLocator) extends Msg(21)

  case class AccountState(adminAccountState: AdminAccountStateDTO) extends Msg(22)

  case class TopUpdated(top: TopDTO) extends Msg(23)

  // enter game 30 - 39

  case class JoinedGame(gameState: GameStateDTO) extends Msg(30)

  case object LeavedGame extends Msg(31)

  // game 40-59

  case class UpdateBuilding(building: BuildingUpdateDTO) extends Msg(40)

  case class UpdateItemStates(states: ItemsStateDTO) extends Msg(41)

  case class AddUnit(unit: UnitDTO) extends Msg(42)

  case class UpdateUnit(unitUpdate: UnitUpdateDTO) extends Msg(43)

  case class RemoveUnit(id: UnitIdDTO) extends Msg(44)

  case class KillUnit(killedId: UnitIdDTO) extends Msg(45)

  case class AddFireball(fireball: FireballDTO) extends Msg(46)

  case class AddVolcano(volcano: VolcanoDTO) extends Msg(47)

  case class AddTornado(tornado: TornadoDTO) extends Msg(48)

  case class AddBullet(bullet: BulletDTO) extends Msg(49)

  case class GameOver(gameOver: GameOverDTO) extends Msg(50)

}
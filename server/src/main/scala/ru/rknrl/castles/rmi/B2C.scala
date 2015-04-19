//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.rmi

import ru.rknrl.castles.rmi.C2B.GameMsg
import ru.rknrl.core.rmi.Msg
import ru.rknrl.dto._

object B2C {

  // auth 0 - 9

  case class Authenticated(success: AuthenticatedDTO) extends Msg(1)

  // admin 10 - 19

  case object AuthenticatedAsAdmin extends Msg(10)

  case class ServerHealth(serverHealth: ServerHealthDTO) extends Msg(11)

  // account 20 - 29

  case class AccountStateUpdated(accountState: AccountStateDTO) extends Msg(20)

  case class EnteredGame(node: NodeLocator) extends Msg(21)

  case class AccountState(adminAccountState: AdminAccountStateDTO) extends Msg(22)

  case class TopUpdated(top: TopDTO) extends Msg(23)

  // enter game 30 - 39

  case class JoinedGame(gameState: GameStateDTO) extends Msg(30)

  case object LeavedGame extends Msg(31)

  // game 40-59

  case class UpdateBuilding(building: BuildingUpdateDTO) extends GameMsg(40)

  case class UpdateItemStates(states: ItemStatesDTO) extends GameMsg(41)

  case class AddUnit(unit: UnitDTO) extends GameMsg(42)

  case class UpdateUnit(unitUpdate: UnitUpdateDTO) extends GameMsg(43)

  case class KillUnit(killedId: UnitId) extends GameMsg(44)

  case class AddFireball(fireball: FireballDTO) extends GameMsg(45)

  case class AddVolcano(volcano: VolcanoDTO) extends GameMsg(46)

  case class AddTornado(tornado: TornadoDTO) extends GameMsg(47)

  case class AddBullet(bullet: BulletDTO) extends GameMsg(48)

  case class GameOver(gameOver: GameOverDTO) extends GameMsg(49)

}
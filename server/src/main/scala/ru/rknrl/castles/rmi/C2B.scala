//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.rmi

import ru.rknrl.core.rmi.Msg
import ru.rknrl.dto.AccountDTO.{BuyBuildingDTO, BuyItemDTO, RemoveBuildingDTO, UpgradeBuildingDTO, UpgradeSkillDTO}
import ru.rknrl.dto.AdminDTO._
import ru.rknrl.dto.AuthDTO.AuthenticateDTO
import ru.rknrl.dto.CommonDTO.{AccountIdDTO, TutorStateDTO}
import ru.rknrl.dto.GameDTO._

object C2B {

  abstract class GameMsg(id: Byte) extends Msg(id)

  // auth

  case class Authenticate(authenticate: AuthenticateDTO) extends Msg(101)

  case class AuthenticateAsAdmin(authenticate: AdminAuthenticateDTO) extends Msg(102)

  // admin

  case class GetAccountState(dto: AdminGetAccountStateDTO) extends Msg(103)

  case object GetOnline extends Msg(104)

  // account

  case class BuyBuilding(buy: BuyBuildingDTO) extends Msg(105)

  case class UpgradeBuilding(id: UpgradeBuildingDTO) extends Msg(106)

  case class RemoveBuilding(id: RemoveBuildingDTO) extends Msg(107)

  case class UpgradeSkill(upgrade: UpgradeSkillDTO) extends Msg(108)

  case class BuyItem(buy: BuyItemDTO) extends Msg(109)

  case object EnterGame extends Msg(110)

  case class UpdateTutorState(tutorState: TutorStateDTO) extends Msg(111)

  // enter game

  case object JoinGame extends GameMsg(112)

  case object Surrender extends GameMsg(113)

  case object LeaveGame extends GameMsg(114)

  // game

  case class Move(dto: MoveDTO) extends GameMsg(115)

  case class CastFireball(point: PointDTO) extends GameMsg(116)

  case class CastStrengthening(buildingId: BuildingIdDTO) extends GameMsg(117)

  case class CastVolcano(point: PointDTO) extends GameMsg(118)

  case class CastTornado(cast: CastTorandoDTO) extends GameMsg(119)

  case class CastAssistance(buildingId: BuildingIdDTO) extends GameMsg(120)

  case object StartTutorGame extends GameMsg(126)

  // admin

  case class AddGold(dto: AdminAddGoldDTO) extends Msg(121)

  case class AddItem(dto: AdminAddItemDTO) extends Msg(122)

  case class SetSkill(dto: AdminSetSkillDTO) extends Msg(123)

  case class SetSlot(dto: AdminSetSlotDTO) extends Msg(124)

  case class DeleteAccount(accountId: AccountIdDTO) extends Msg(125)
}

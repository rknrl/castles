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

  case class Authenticate(authenticate: AuthenticateDTO) extends Msg(65)

  case class AuthenticateAsAdmin(authenticate: AdminAuthenticateDTO) extends Msg(66)

  // admin

  case class GetAccountState(dto: AdminGetAccountStateDTO) extends Msg(67)

  case object GetOnline extends Msg(68)

  case class AddGold(dto: AdminAddGoldDTO) extends Msg(69)

  case class AddItem(dto: AdminAddItemDTO) extends Msg(70)

  case class SetSkill(dto: AdminSetSkillDTO) extends Msg(71)

  case class SetSlot(dto: AdminSetSlotDTO) extends Msg(72)

  case class DeleteAccount(accountId: AccountIdDTO) extends Msg(73)

  // account

  case class BuyBuilding(buy: BuyBuildingDTO) extends Msg(74)

  case class UpgradeBuilding(id: UpgradeBuildingDTO) extends Msg(75)

  case class RemoveBuilding(id: RemoveBuildingDTO) extends Msg(76)

  case class UpgradeSkill(upgrade: UpgradeSkillDTO) extends Msg(77)

  case class BuyItem(buy: BuyItemDTO) extends Msg(78)

  case object EnterGame extends Msg(79)

  case class UpdateTutorState(tutorState: TutorStateDTO) extends Msg(80)

  // enter game

  case object JoinGame extends GameMsg(81)

  case object Surrender extends GameMsg(82)

  case object LeaveGame extends GameMsg(83)

  // game

  case class Move(dto: MoveDTO) extends GameMsg(84)

  case class CastFireball(point: PointDTO) extends GameMsg(85)

  case class CastStrengthening(buildingId: BuildingIdDTO) extends GameMsg(86)

  case class CastVolcano(point: PointDTO) extends GameMsg(87)

  case class CastTornado(cast: CastTorandoDTO) extends GameMsg(88)

  case class CastAssistance(buildingId: BuildingIdDTO) extends GameMsg(89)

  case object StartTutorFireball extends GameMsg(90)

  case object StartTutorGame extends GameMsg(91)

}

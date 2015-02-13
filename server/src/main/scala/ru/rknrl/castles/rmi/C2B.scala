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
import ru.rknrl.dto.GameDTO._

object C2B {

  trait GameMsg extends Msg

  // auth

  case class Authenticate(authenticate: AuthenticateDTO) extends Msg

  case class AuthenticateAsAdmin(authenticate: AdminAuthenticateDTO) extends Msg

  // admin

  case class GetAccountState(dto: AdminGetAccountStateDTO) extends Msg

  // account

  case class BuyBuilding(buy: BuyBuildingDTO) extends Msg

  case class UpgradeBuilding(id: UpgradeBuildingDTO) extends Msg

  case class RemoveBuilding(id: RemoveBuildingDTO) extends Msg

  case class UpgradeSkill(upgrade: UpgradeSkillDTO) extends Msg

  case class BuyItem(buy: BuyItemDTO) extends Msg

  case object EnterGame extends Msg

  // enter game

  case object JoinGame extends GameMsg

  case object Surrender extends GameMsg

  case object LeaveGame extends GameMsg

  // game

  case class Move(dto: MoveDTO) extends GameMsg

  case class CastFireball(point: PointDTO) extends GameMsg

  case class CastStrengthening(buildingId: BuildingIdDTO) extends GameMsg

  case class CastVolcano(point: PointDTO) extends GameMsg

  case class CastTornado(cast: CastTorandoDTO) extends GameMsg

  case class CastAssistance(buildingId: BuildingIdDTO) extends GameMsg

  // admin

  case class AddGold(dto: AdminAddGoldDTO) extends Msg

  case class AddItem(dto: AdminAddItemDTO) extends Msg

  case class SetSkill(dto: AdminSetSkillDTO) extends Msg

  case class SetSlot(dto: AdminSetSlotDTO) extends Msg

}

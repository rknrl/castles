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

  // auth 60 - 69

  case class Authenticate(authenticate: AuthenticateDTO) extends Msg(60)

  case class AuthenticateAsAdmin(authenticate: AdminAuthenticateDTO) extends Msg(61)

  // admin 70 - 79

  case class GetAccountState(dto: AdminGetAccountStateDTO) extends Msg(70)

  case object GetOnline extends Msg(71)

  case class AddGold(dto: AdminAddGoldDTO) extends Msg(72)

  case class AddItem(dto: AdminAddItemDTO) extends Msg(73)

  case class SetSkill(dto: AdminSetSkillDTO) extends Msg(74)

  case class SetSlot(dto: AdminSetSlotDTO) extends Msg(75)

  case class DeleteAccount(accountId: AccountIdDTO) extends Msg(76)

  // account 80 - 89

  case class BuyBuilding(buy: BuyBuildingDTO) extends Msg(80)

  case class UpgradeBuilding(id: UpgradeBuildingDTO) extends Msg(81)

  case class RemoveBuilding(id: RemoveBuildingDTO) extends Msg(82)

  case class UpgradeSkill(upgrade: UpgradeSkillDTO) extends Msg(83)

  case class BuyItem(buy: BuyItemDTO) extends Msg(84)

  case object EnterGame extends Msg(85)

  case class UpdateTutorState(tutorState: TutorStateDTO) extends Msg(86)

  // enter game 90 - 99

  case object JoinGame extends GameMsg(90)

  case object Surrender extends GameMsg(91)

  case object LeaveGame extends GameMsg(92)

  // game 100 - 119

  case class Move(dto: MoveDTO) extends GameMsg(100)

  case class CastFireball(point: PointDTO) extends GameMsg(101)

  case class CastStrengthening(buildingId: BuildingIdDTO) extends GameMsg(102)

  case class CastVolcano(point: PointDTO) extends GameMsg(103)

  case class CastTornado(cast: CastTorandoDTO) extends GameMsg(104)

  case class CastAssistance(buildingId: BuildingIdDTO) extends GameMsg(105)

  case object StartTutorFireball extends GameMsg(106)

  case object StartTutorGame extends GameMsg(107)

}

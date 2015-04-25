//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castlesbot

import akka.actor.{Actor, ActorRef}
import akka.io.Tcp.Connected
import org.slf4j.LoggerFactory
import ru.rknrl.RandomUtil.random
import ru.rknrl.castles.account.AccountConfig
import ru.rknrl.castles.rmi.B2C._
import ru.rknrl.castles.rmi.C2B._
import ru.rknrl.dto.AccountType.DEV
import ru.rknrl.dto.BuildingLevel.{LEVEL_1, LEVEL_3}
import ru.rknrl.dto.DeviceType.PC
import ru.rknrl.dto.PlatformType.CANVAS
import ru.rknrl.dto.SkillLevel.SKILL_LEVEL_3
import ru.rknrl.dto._
import ru.rknrl.{Logged, Slf4j}

object AccountAction extends Enumeration {
  type AccountAction = Value
  val BUY_BUILDING = Value
  val UPGRADE_BUILDING = Value
  val REMOVE_BUILDING = Value
  val BUY_ITEM = Value
  val UPGRADE_SKILL = Value
  val BUY_STARS = Value
  val ENTER_GAME = Value
}

import ru.rknrl.castlesbot.AccountAction._

class CastlesBot(server: ActorRef) extends Actor {
  val log = new Slf4j(LoggerFactory.getLogger(getClass))

  var _config: Option[AccountConfigDTO] = None
  var _accountState: Option[AccountStateDTO] = None
  var gameState: Option[GameStateDTO] = None

  def config = _config.get

  def accountState = _accountState.get

  def logged(r: Receive) = new Logged(r, log, None, None, any ⇒ true)

  def receive = logged {
    case _: Connected ⇒
      val accountId = AccountId(DEV, "1")
      send(Authenticate(AuthenticateDTO(UserInfoDTO(accountId), CANVAS, PC, AuthenticationSecretDTO(""))))

    case Authenticated(dto) ⇒
      _config = Some(dto.config)
      _accountState = Some(dto.accountState)

      if (dto.game.isDefined) {
        send(JoinGame)
        context become inGame
      } else if (dto.searchOpponents) {
        context become enterGame
      } else {
        context become inMenu
      }
  }

  def inMenu: Receive = logged({
    case AccountStateUpdated(newAccountState) ⇒
      _accountState = Some(newAccountState)

    case TopUpdated(top) ⇒
  })

  def accountAction(): Unit = {
    val action = random(AccountAction.values.toSeq)

    action match {
      case BUY_BUILDING ⇒
        if (accountState.gold >= buildingPrice(LEVEL_1)) {
          val emptySlots = accountState.slots.filter(_.buildingPrototype.isEmpty)
          if (emptySlots.size > 0) {
            val slotId = random(emptySlots).id
            val buildingType = random(BuildingType.values)
            send(BuyBuilding(BuyBuildingDTO(slotId, buildingType)))
          }
        }

      case UPGRADE_BUILDING ⇒
        val upgradableSlots = accountState.slots.filter(s ⇒
          s.buildingPrototype.isDefined &&
            s.buildingPrototype.get.buildingLevel != LEVEL_3 &&
            accountState.gold >= buildingPrice(AccountConfig.nextBuildingLevel(s.buildingPrototype.get.buildingLevel))
        )
        if (upgradableSlots.size > 0) {
          val slotId = random(upgradableSlots).id
          send(UpgradeBuilding(UpgradeBuildingDTO(slotId)))
        }

      case REMOVE_BUILDING ⇒
        val notEmptySlots = accountState.slots.filter(_.buildingPrototype.isDefined)
        if (notEmptySlots.size > 0) {
          val slotId = random(notEmptySlots).id
          send(RemoveBuilding(RemoveBuildingDTO(slotId)))
        }

      case BUY_ITEM ⇒
        if (accountState.gold >= config.itemPrice) {
          val itemType = random(ItemType.values)
          send(BuyItem(BuyItemDTO(itemType)))
        }

      case UPGRADE_SKILL ⇒
        val totalLevel = getTotalLevel(accountState.skills)
        if (totalLevel < 9 && accountState.gold >= skillUpgradePrice(totalLevel)) {
          val upgradableSkills = accountState.skills.filter(_.level != SKILL_LEVEL_3)
          val skillType = random(upgradableSkills).skillType
          send(UpgradeSkill(UpgradeSkillDTO(skillType)))
        }

      case BUY_STARS ⇒
      // todo

      case ENTER_GAME ⇒
        send(EnterGame)
        context become enterGame
    }
  }

  def enterGame: Receive = logged({
    case EnteredGame(node) ⇒
      send(JoinGame)

    case JoinedGame(newGameState) ⇒
      gameState = Some(newGameState)
      context become inGame
  })

  def inGame: Receive = logged({
    case GameStateUpdated(gameStateUpdate) ⇒

    case GameOver(gameOver) ⇒

    case LeavedGame ⇒
  })

  def send(msg: Any): Unit = {
    log.debug("send " + msg)
    server ! msg
  }

  def getTotalLevel(skillLevels: Seq[SkillLevelDTO]) = {
    var total = 0
    for (skillLevel ← skillLevels) total += skillLevel.level.id
    total
  }

  def skillUpgradePrice(totalLevel: Int) =
    config.skillUpgradePrices.find(_.totalLevel == totalLevel).get.price

  def buildingPrice(level: BuildingLevel) =
    config.buildings.find(_.level == level).get.price
}

//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castlesbot

import akka.actor.{Actor, ActorRef, Props}
import akka.io.Tcp.Connected
import protos.BuildingLevel.{LEVEL_1, LEVEL_3}
import protos.DeviceType.PC
import protos.PlatformType.CANVAS
import protos.SkillLevel.SKILL_LEVEL_3
import protos._
import ru.rknrl.RandomUtil.random
import ru.rknrl.Supervisor._
import ru.rknrl.castles.account.AccountConfig
import ru.rknrl.castles.bot.GameBot
import ru.rknrl.castles.matchmaking.MatchMaking.ConnectToGame
import ru.rknrl.logging.ActorLog

import scala.concurrent.duration._

object MenuAction extends Enumeration {
  type AccountAction = Value
  val BUY_BUILDING = Value
  val UPGRADE_BUILDING = Value
  val REMOVE_BUILDING = Value
  val BUY_ITEM = Value
  val UPGRADE_SKILL = Value
  val BUY_STARS = Value
  val ENTER_GAME = Value
}

import ru.rknrl.castlesbot.MenuAction._

object Bot {
  def props(server: ActorRef, accountId: AccountId) =
    Props(classOf[Bot], server, accountId)
}

class Bot(server: ActorRef, accountId: AccountId) extends Actor with ActorLog {

  override def supervisorStrategy = EscalateStrategy

  override val logFilter: Any ⇒ Boolean = {
    case state: GameStateUpdate ⇒ false
    case DoMenuAction ⇒ false
    case _ ⇒ true
  }

  var _config: Option[protos.AccountConfig] = None
  var _accountState: Option[AccountState] = None
  var gameBot: Option[ActorRef] = None

  def config = _config.get

  def accountState = _accountState.get

  import context.dispatcher

  case object DoMenuAction

  val interval = 3 second
  val scheduler = context.system.scheduler.schedule(interval, interval, self, DoMenuAction)

  var waitForAccountStateUpdate = false

  def receive = logged {
    case _: Connected ⇒
      send(Authenticate(UserInfo(accountId), CANVAS, PC, AuthenticationSecret("")))

    case dto: Authenticated ⇒
      _config = Some(dto.config)
      _accountState = Some(dto.accountState)

      if (dto.game.isDefined) {
        send(JoinGame())
        context become enterGame
      } else if (dto.searchOpponents) {
        context become enterGame
      } else {
        if (dto.lastWeekTop.isDefined) {
          waitForAccountStateUpdate = true
          send(AcceptWeekTop(WeekNumber(dto.lastWeekTop.get.weekNumber)))
        }
        context become inMenu
      }
  }

  def persistent: Receive = logged {
    case newAccountState: AccountState ⇒
      _accountState = Some(newAccountState)
      waitForAccountStateUpdate = false

    case top: protos.Top ⇒
  }

  def inMenu: Receive = logged {
    case DoMenuAction ⇒
      val action = random(MenuAction.values.toSeq)

      if (!waitForAccountStateUpdate)
        action match {
          case BUY_BUILDING ⇒
            if (accountState.gold >= buildingPrice(LEVEL_1)) {
              val emptySlots = accountState.slots.filter(_.buildingPrototype.isEmpty)
              if (emptySlots.nonEmpty) {
                val slotId = random(emptySlots).id
                val buildingType = random(BuildingType.values)
                waitForAccountStateUpdate = true
                send(BuyBuilding(slotId, buildingType))
              }
            }

          case UPGRADE_BUILDING ⇒
            val upgradableSlots = accountState.slots.filter(s ⇒
              s.buildingPrototype.isDefined &&
                s.buildingPrototype.get.buildingLevel != LEVEL_3 &&
                accountState.gold >= buildingPrice(AccountConfig.nextBuildingLevel(s.buildingPrototype.get.buildingLevel))
            )
            if (upgradableSlots.nonEmpty) {
              val slotId = random(upgradableSlots).id
              waitForAccountStateUpdate = true
              send(UpgradeBuilding(slotId))
            }

          case REMOVE_BUILDING ⇒
            val notEmptySlots = accountState.slots.filter(_.buildingPrototype.isDefined)
            if (notEmptySlots.size > 1) {
              val slotId = random(notEmptySlots).id
              waitForAccountStateUpdate = true
              send(RemoveBuilding(slotId))
            }

          case BUY_ITEM ⇒
            if (accountState.gold >= config.itemPrice) {
              val itemType = random(ItemType.values)
              waitForAccountStateUpdate = true
              send(BuyItem(itemType))
            }

          case UPGRADE_SKILL ⇒
            val totalLevel = AccountConfig.getTotalLevel(accountState.skills)
            if (totalLevel < 9 && accountState.gold >= skillUpgradePrice(totalLevel + 1)) {
              val upgradableSkills = accountState.skills.filter(_.level != SKILL_LEVEL_3)
              val skillType = random(upgradableSkills).skillType
              waitForAccountStateUpdate = true
              send(UpgradeSkill(skillType))
            }

          case BUY_STARS ⇒ // todo

          case ENTER_GAME ⇒
            send(EnterGame())
            context become enterGame
        }
  } orElse persistent

  def enterGame: Receive = logged {
    case EnteredGame(node) ⇒
      send(JoinGame())

    case msg: GameState ⇒
      gameBot = Some(context.actorOf(Props(classOf[GameBot], accountId), "game-bot-" + accountId.id))
      gameBot.get ! ConnectToGame(sender)
      gameBot.get forward msg
      context become inGame
  } orElse persistent

  def inGame: Receive = logged {
    case msg: GameStateUpdate ⇒
      gameBot.get forward msg

    case msg: GameOver ⇒
      gameBot.get forward msg

    case LeavedGame() ⇒
      context stop gameBot.get
      waitForAccountStateUpdate = true
      context become inMenu
  } orElse persistent

  def send(msg: Any): Unit = send(server, msg)

  def skillUpgradePrice(totalLevel: Int) =
    config.skillUpgradePrices.find(_.totalLevel == totalLevel).get.price

  def buildingPrice(level: BuildingLevel) =
    config.buildings.find(_.level == level).get.price
}

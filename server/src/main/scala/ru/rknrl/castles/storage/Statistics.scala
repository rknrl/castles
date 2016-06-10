//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.storage

import protos.BuildingLevel.{LEVEL_1, LEVEL_2, LEVEL_3}
import protos.BuildingType.{CHURCH, HOUSE, TOWER}
import protos.ItemType._
import protos.SkillLevel.{SKILL_LEVEL_1, SKILL_LEVEL_2, SKILL_LEVEL_3}
import protos.SkillType.{ATTACK, DEFENCE, SPEED}
import protos.StatAction._
import protos._
import ru.rknrl.castles.matchmaking.MatchMaking.GameOrder

object Statistics {
  def buyItem(itemType: ItemType) =
    itemType match {
      case FIREBALL ⇒ BUY_FIREBALL
      case STRENGTHENING ⇒ BUY_STRENGTHENING
      case VOLCANO ⇒ BUY_VOLCANO
      case TORNADO ⇒ BUY_TORNADO
      case ASSISTANCE ⇒ BUY_ASSISTANCE
    }

  def buyBuilding(prototype: BuildingPrototype): StatAction =
    buyBuilding(prototype.buildingType, prototype.buildingLevel)

  def buyBuilding(buildingType: BuildingType, buildingLevel: BuildingLevel): StatAction =
    buildingType match {
      case HOUSE ⇒
        buildingLevel match {
          case LEVEL_1 ⇒ BUY_HOUSE1
          case LEVEL_2 ⇒ BUY_HOUSE2
          case LEVEL_3 ⇒ BUY_HOUSE3
        }
      case TOWER ⇒
        buildingLevel match {
          case LEVEL_1 ⇒ BUY_TOWER1
          case LEVEL_2 ⇒ BUY_TOWER2
          case LEVEL_3 ⇒ BUY_TOWER3
        }
      case CHURCH ⇒
        buildingLevel match {
          case LEVEL_1 ⇒ BUY_CHURCH1
          case LEVEL_2 ⇒ BUY_CHURCH2
          case LEVEL_3 ⇒ BUY_CHURCH3
        }
    }

  def buySkill(skillType: SkillType, skillLevel: SkillLevel) =
    skillType match {
      case ATTACK ⇒
        skillLevel match {
          case SKILL_LEVEL_1 ⇒ BUY_ATTACK1
          case SKILL_LEVEL_2 ⇒ BUY_ATTACK2
          case SKILL_LEVEL_3 ⇒ BUY_ATTACK3
        }
      case DEFENCE ⇒
        skillLevel match {
          case SKILL_LEVEL_1 ⇒ BUY_DEFENCE1
          case SKILL_LEVEL_2 ⇒ BUY_DEFENCE2
          case SKILL_LEVEL_3 ⇒ BUY_DEFENCE3
        }
      case SPEED ⇒
        skillLevel match {
          case SKILL_LEVEL_1 ⇒ BUY_SPEED1
          case SKILL_LEVEL_2 ⇒ BUY_SPEED2
          case SKILL_LEVEL_3 ⇒ BUY_SPEED3
        }
    }

  def leaveGameStat(place: Int,
                    isTutor: Boolean,
                    orders: Iterable[GameOrder],
                    order: GameOrder): Option[StatAction] = {
    if (!order.isBot) {
      val gameWithBots = orders.count(_.isBot) == orders.size - 1
      if (gameWithBots) {
        if (orders.size == 4) {
          if (place == 1) {
            if (isTutor)
              return Some(TUTOR_4_WIN)
            else
              return Some(WIN_4_BOTS)
          } else {
            if (isTutor)
              return Some(TUTOR_4_LOSE)
            else
              return Some(LOSE_4_BOTS)
          }
        } else if (orders.size == 2) {
          if (place == 1) {
            if (isTutor)
              return Some(TUTOR_2_WIN)
            else
              return Some(WIN_2_BOTS)
          } else {
            if (isTutor)
              return Some(TUTOR_2_LOSE)
            else
              return Some(LOSE_2_BOTS)
          }
        }
      }
    }

    None
  }

  def createGameStat(orders: Iterable[GameOrder]): StatAction = {
    if (orders.count(_.isBot) == orders.size - 1) {
      if (orders.size == 4)
        START_GAME_4_WITH_BOTS
      else
        START_GAME_2_WITH_BOTS
    } else {
      if (orders.size == 4)
        START_GAME_4_WITH_PLAYERS
      else
        START_GAME_2_WITH_PLAYERS
    }
  }
}

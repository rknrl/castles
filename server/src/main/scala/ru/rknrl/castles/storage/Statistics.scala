//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.storage

import akka.actor.ActorRef
import protos._
import ru.rknrl.castles.matchmaking.MatchMaking.GameOrder

object Statistics {
  def buyItem(itemType: ItemType) =
    itemType match {
      case ItemType.FIREBALL ⇒ StatAction.BUY_FIREBALL
      case ItemType.STRENGTHENING ⇒ StatAction.BUY_STRENGTHENING
      case ItemType.VOLCANO ⇒ StatAction.BUY_VOLCANO
      case ItemType.TORNADO ⇒ StatAction.BUY_TORNADO
      case ItemType.ASSISTANCE ⇒ StatAction.BUY_ASSISTANCE
    }

  def buyBuilding(prototype: BuildingPrototype): StatAction =
    buyBuilding(prototype.buildingType, prototype.buildingLevel)

  def buyBuilding(buildingType: BuildingType, buildingLevel: BuildingLevel): StatAction =
    buildingType match {
      case BuildingType.HOUSE ⇒
        buildingLevel match {
          case BuildingLevel.LEVEL_1 ⇒ StatAction.BUY_HOUSE1
          case BuildingLevel.LEVEL_2 ⇒ StatAction.BUY_HOUSE2
          case BuildingLevel.LEVEL_3 ⇒ StatAction.BUY_HOUSE3
        }
      case BuildingType.TOWER ⇒
        buildingLevel match {
          case BuildingLevel.LEVEL_1 ⇒ StatAction.BUY_TOWER1
          case BuildingLevel.LEVEL_2 ⇒ StatAction.BUY_TOWER2
          case BuildingLevel.LEVEL_3 ⇒ StatAction.BUY_TOWER3
        }
      case BuildingType.CHURCH ⇒
        buildingLevel match {
          case BuildingLevel.LEVEL_1 ⇒ StatAction.BUY_CHURCH1
          case BuildingLevel.LEVEL_2 ⇒ StatAction.BUY_CHURCH2
          case BuildingLevel.LEVEL_3 ⇒ StatAction.BUY_CHURCH3
        }
    }

  def buySkill(skillType: SkillType, skillLevel: SkillLevel) =
    skillType match {
      case SkillType.ATTACK ⇒
        skillLevel match {
          case SkillLevel.SKILL_LEVEL_1 ⇒ StatAction.BUY_ATTACK1
          case SkillLevel.SKILL_LEVEL_2 ⇒ StatAction.BUY_ATTACK2
          case SkillLevel.SKILL_LEVEL_3 ⇒ StatAction.BUY_ATTACK3
        }
      case SkillType.DEFENCE ⇒
        skillLevel match {
          case SkillLevel.SKILL_LEVEL_1 ⇒ StatAction.BUY_DEFENCE1
          case SkillLevel.SKILL_LEVEL_2 ⇒ StatAction.BUY_DEFENCE2
          case SkillLevel.SKILL_LEVEL_3 ⇒ StatAction.BUY_DEFENCE3
        }
      case SkillType.SPEED ⇒
        skillLevel match {
          case SkillLevel.SKILL_LEVEL_1 ⇒ StatAction.BUY_SPEED1
          case SkillLevel.SKILL_LEVEL_2 ⇒ StatAction.BUY_SPEED2
          case SkillLevel.SKILL_LEVEL_3 ⇒ StatAction.BUY_SPEED3
        }
    }


  // todo: send logging
  def sendLeaveGameStatistics(place: Int,
                              isTutor: Boolean,
                              orders: Iterable[GameOrder],
                              order: GameOrder,
                              graphite: ActorRef)
                             (implicit sender: ActorRef): Unit = {
    if (!order.isBot) {
      val gameWithBots = orders.count(_.isBot) == orders.size - 1
      if (gameWithBots) {
        if (orders.size == 4) {
          if (place == 1) {
            if (isTutor)
              graphite ! StatAction.TUTOR_4_WIN
            else
              graphite ! StatAction.WIN_4_BOTS
          } else {
            if (isTutor)
              graphite ! StatAction.TUTOR_4_LOSE
            else
              graphite ! StatAction.LOSE_4_BOTS
          }
        } else if (orders.size == 2) {
          if (place == 1) {
            if (isTutor)
              graphite ! StatAction.TUTOR_2_WIN
            else
              graphite ! StatAction.WIN_2_BOTS
          } else {
            if (isTutor)
              graphite ! StatAction.TUTOR_2_LOSE
            else
              graphite ! StatAction.LOSE_2_BOTS
          }
        }
      }
    }
  }

  // todo: send logging
  def sendCreateGameStatistics(orders: Iterable[GameOrder],
                               graphite: ActorRef)
                              (implicit sender: ActorRef): Unit = {
    if (orders.count(_.isBot) == orders.size - 1) {
      if (orders.size == 4)
        graphite ! StatAction.START_GAME_4_WITH_BOTS
      else
        graphite ! StatAction.START_GAME_2_WITH_BOTS
    } else {
      if (orders.size == 4)
        graphite ! StatAction.START_GAME_4_WITH_PLAYERS
      else
        graphite ! StatAction.START_GAME_2_WITH_PLAYERS
    }
  }
}

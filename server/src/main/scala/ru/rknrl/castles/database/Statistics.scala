//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.database

import ru.rknrl.castles.rmi.C2B.UpdateStatistics
import ru.rknrl.dto.CommonDTO._

object Statistics {
  def updateStatistics(action: StatAction) = UpdateStatistics(StatDTO.newBuilder.setAction(action).build)

  def buyItem(itemType: ItemType) =
    itemType match {
      case ItemType.FIREBALL ⇒ StatAction.BUY_FIREBALL
      case ItemType.STRENGTHENING ⇒ StatAction.BUY_STRENGTHENING
      case ItemType.VOLCANO ⇒ StatAction.BUY_VOLCANO
      case ItemType.TORNADO ⇒ StatAction.BUY_TORNADO
      case ItemType.ASSISTANCE ⇒ StatAction.BUY_ASSISTANCE
    }

  def buyBuilding(prototype: BuildingPrototypeDTO): StatAction =
    buyBuilding(prototype.getType, prototype.getLevel)

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
}

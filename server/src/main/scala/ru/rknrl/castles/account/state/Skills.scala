//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account.state

import ru.rknrl.castles.account.AccountConfig
import ru.rknrl.castles.game.GameConfig
import ru.rknrl.castles.game.state.Stat
import ru.rknrl.dto.AccountDTO.SkillLevelDTO
import ru.rknrl.dto.CommonDTO.{SkillLevel, SkillType}

class Skills(val levels: Map[SkillType, SkillLevel]) {
  for (skillType ← SkillType.values()) assert(levels.contains(skillType))

  def set(skillType: SkillType, skillLevel: SkillLevel) =
    new Skills(levels.updated(skillType, skillLevel))

  def upgrade(skillType: SkillType) = {
    val nextLevel = Skills.getNextLevel(levels(skillType))
    new Skills(levels.updated(skillType, nextLevel))
  }

  def totalLevel = {
    var total = 0
    for ((skillType, level) ← levels) total += level.getNumber
    total
  }

  def isLastTotalLevel = totalLevel == 9

  def nextTotalLevel = {
    assert(!isLastTotalLevel)
    totalLevel + 1
  }

  def dto =
    for ((skillType, level) ← levels)
    yield SkillLevelDTO.newBuilder()
      .setType(skillType)
      .setLevel(level)
      .build()

  val levelsCount = SkillLevel.values().size - 1

  def stat(config: AccountConfig) =
    new Stat(
      1 + levels(SkillType.ATTACK).getNumber * config.maxAttack / levelsCount,
      1 + levels(SkillType.DEFENCE).getNumber * config.maxDefence / levelsCount,
      1 + levels(SkillType.SPEED).getNumber * config.maxSpeed / levelsCount
    )
}

object Skills {
  def getNextLevel(level: SkillLevel) =
    level match {
      case SkillLevel.SKILL_LEVEL_0 ⇒ SkillLevel.SKILL_LEVEL_1
      case SkillLevel.SKILL_LEVEL_1 ⇒ SkillLevel.SKILL_LEVEL_2
      case SkillLevel.SKILL_LEVEL_2 ⇒ SkillLevel.SKILL_LEVEL_3
      case _ ⇒ throw new IllegalStateException("hasn't next level " + level)
    }

  def apply(dto: Iterable[SkillLevelDTO]) = {
    val skills = for (skillDto ← dto) yield skillDto.getType → skillDto.getLevel
    new Skills(skills.toMap)
  }
}

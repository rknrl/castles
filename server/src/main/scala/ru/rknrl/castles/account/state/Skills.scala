//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account.state

import ru.rknrl.Assertion
import ru.rknrl.castles.account.AccountConfig
import ru.rknrl.castles.game.state.Stat
import ru.rknrl.dto.AccountDTO.SkillLevelDTO
import ru.rknrl.dto.CommonDTO.{SkillLevel, SkillType}

object Skills {
  type Skills = Map[SkillType, SkillLevel]

  def nextLevel(level: SkillLevel) =
    level match {
      case SkillLevel.SKILL_LEVEL_0 ⇒ SkillLevel.SKILL_LEVEL_1
      case SkillLevel.SKILL_LEVEL_1 ⇒ SkillLevel.SKILL_LEVEL_2
      case SkillLevel.SKILL_LEVEL_2 ⇒ SkillLevel.SKILL_LEVEL_3
    }

  def apply(dto: Iterable[SkillLevelDTO]) = {
    val skills = for (skillDto ← dto) yield skillDto.getType → skillDto.getLevel
    skills.toMap
  }

  def totalLevel(levels: Skills) = {
    var total = 0
    for ((skillType, level) ← levels) total += level.getNumber
    total
  }

  def isLastTotalLevel(levels: Skills) = totalLevel(levels) == 9

  def nextTotalLevel(levels: Skills) = {
    Assertion.check(!isLastTotalLevel(levels))
    totalLevel(levels) + 1
  }

  val levelsCount = SkillLevel.values.size - 1

  def stat(config: AccountConfig, levels: Skills) =
    new Stat(
      1 + levels(SkillType.ATTACK).getNumber * config.maxAttack / levelsCount,
      1 + levels(SkillType.DEFENCE).getNumber * config.maxDefence / levelsCount,
      1 + levels(SkillType.SPEED).getNumber * config.maxSpeed / levelsCount
    )

  def dto(levels: Skills) =
    for ((skillType, level) ← levels)
      yield SkillLevelDTO.newBuilder
        .setType(skillType)
        .setLevel(level)
        .build

}

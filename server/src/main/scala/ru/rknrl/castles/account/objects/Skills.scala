package ru.rknrl.castles.account.objects

import ru.rknrl.castles.game.Stat
import ru.rknrl.dto.AccountDTO.{SkillLevelDTO, SkillsDTO}
import ru.rknrl.dto.CommonDTO.{SkillLevel, SkillType}

import scala.collection.JavaConverters._

class Skills(val levels: Map[SkillType, SkillLevel]) {
  for (skillType ← SkillType.values()) assert(levels.contains(skillType))

  def upgrade(skillType: SkillType) = {
    val nextLevel = Skills.getNextLevel(levels(skillType))
    new Skills(levels.updated(skillType, nextLevel))
  }

  def upgradePrice = {
    var total = 0
    for ((skillType, level) ← levels) {
      total += Skills.levelToInt(level)
    }
    Math.pow(2, total).toInt
  }

  private def levelsDto =
    for ((skillType, level) ← levels)
    yield SkillLevelDTO.newBuilder()
      .setType(skillType)
      .setLevel(level)
      .build()

  def dto = SkillsDTO.newBuilder()
    .addAllLevels(levelsDto.asJava)
    .build()

  def stat =
    new Stat(
      levels(SkillType.ATTACK).getNumber,
      levels(SkillType.DEFENCE).getNumber,
      levels(SkillType.SPEED).getNumber
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

  def levelToInt(level: SkillLevel) = level.getNumber
}

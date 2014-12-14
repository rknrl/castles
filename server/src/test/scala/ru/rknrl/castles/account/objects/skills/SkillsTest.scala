package ru.rknrl.castles.account.objects.skills

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.account.objects.Skills
import ru.rknrl.dto.AccountDTO.{SkillLevelDTO, SkillsDTO}
import ru.rknrl.dto.CommonDTO.{SkillLevel, SkillType}

object SkillsTest {
  val attack = SkillType.ATTACK → SkillLevel.SKILL_LEVEL_1
  val defence = SkillType.DEFENCE → SkillLevel.SKILL_LEVEL_0
  val speed = SkillType.SPEED → SkillLevel.SKILL_LEVEL_3

  val skills = new Skills(Map(attack, defence, speed))
}

class SkillsTest extends FlatSpec with Matchers {

  import ru.rknrl.castles.account.objects.skills.SkillsTest._

  it should "throw AssertionError if not contains all skills" in {
    a[AssertionError] should be thrownBy {
      new Skills(Map(attack, defence))
    }
  }

  "upgrade" should "throw Error if last level already reached" in {
    a[Exception] should be thrownBy {
      skills.upgrade(SkillType.SPEED)
    }
  }

  "upgrade" should "change level & not change others" in {
    val updated = skills.upgrade(SkillType.DEFENCE).levels
    updated(SkillType.ATTACK) should be(SkillLevel.SKILL_LEVEL_1)
    updated(SkillType.DEFENCE) should be(SkillLevel.SKILL_LEVEL_1)
    updated(SkillType.SPEED) should be(SkillLevel.SKILL_LEVEL_3)
  }

  "dto" should "be correct" in {
    checkDto(skills, skills.dto)
  }

  def checkDto(skills: Skills, dto: SkillsDTO) {
    dto.getLevelsCount should be(3)

    getLevel(SkillType.ATTACK).getLevel should be(SkillLevel.SKILL_LEVEL_1)
    getLevel(SkillType.DEFENCE).getLevel should be(SkillLevel.SKILL_LEVEL_0)
    getLevel(SkillType.SPEED).getLevel should be(SkillLevel.SKILL_LEVEL_3)

    def getLevel(skillType: SkillType): SkillLevelDTO = {
      for (i ← 0 until dto.getLevelsCount)
        if (dto.getLevels(i).getType == skillType)
          return dto.getLevels(i)
      throw new IllegalStateException()
    }
  }

  "getNextLevel" should "return level" in {
    Skills.getNextLevel(SkillLevel.SKILL_LEVEL_0) should be(SkillLevel.SKILL_LEVEL_1)
    Skills.getNextLevel(SkillLevel.SKILL_LEVEL_1) should be(SkillLevel.SKILL_LEVEL_2)
    Skills.getNextLevel(SkillLevel.SKILL_LEVEL_2) should be(SkillLevel.SKILL_LEVEL_3)
  }

  "getNextLevel" should "throw Exception on last level" in {
    a[Exception] should be thrownBy {
      Skills.getNextLevel(SkillLevel.SKILL_LEVEL_3)
    }
  }

  "levelToInt" should "return int" in {
    Skills.levelToInt(SkillLevel.SKILL_LEVEL_0) should be(0)
    Skills.levelToInt(SkillLevel.SKILL_LEVEL_1) should be(1)
    Skills.levelToInt(SkillLevel.SKILL_LEVEL_2) should be(2)
    Skills.levelToInt(SkillLevel.SKILL_LEVEL_3) should be(3)
  }
}

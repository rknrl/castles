//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account.state

import org.scalatest.{Matchers, WordSpec}
import ru.rknrl.castles.kit.Mocks
import ru.rknrl.dto.SkillLevel._
import ru.rknrl.dto.SkillLevelDTO
import ru.rknrl.dto.SkillType._

class AccountConfigTest extends WordSpec with Matchers {
  "skillsToStat" in {
    val skills = List(
      SkillLevelDTO(ATTACK, SKILL_LEVEL_0),
      SkillLevelDTO(DEFENCE, SKILL_LEVEL_2),
      SkillLevelDTO(SPEED, SKILL_LEVEL_3)
    )

    val config = Mocks.accountConfigMock(
      maxAttack = 2,
      maxDefence = 5.2,
      maxSpeed = 1.6
    )

    val stat = config.skillsToStat(skills)

    stat.attack shouldBe (1.0 +- 0.0001)
    stat.defence shouldBe (3.8 +- 0.0001)
    stat.speed shouldBe (1.6 +- 0.0001)
  }
}

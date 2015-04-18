//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.menu.skills {
import org.flexunit.asserts.assertEquals;
import org.flexunit.asserts.assertFalse;
import org.flexunit.asserts.assertTrue;

import ru.rknrl.castles.model.DtoMock;
import ru.rknrl.dto.SkillLevel;
import ru.rknrl.dto.SkillLevelDTO;
import ru.rknrl.dto.SkillType;

public class SkillLevelsTest {
    private const start:SkillLevels = new SkillLevels(new <SkillLevelDTO>[
        DtoMock.skillLevel(SkillType.ATTACK, SkillLevel.SKILL_LEVEL_0),
        DtoMock.skillLevel(SkillType.DEFENCE, SkillLevel.SKILL_LEVEL_0),
        DtoMock.skillLevel(SkillType.SPEED, SkillLevel.SKILL_LEVEL_0)
    ]);

    private const middle:SkillLevels = new SkillLevels(new <SkillLevelDTO>[
        DtoMock.skillLevel(SkillType.ATTACK, SkillLevel.SKILL_LEVEL_1),
        DtoMock.skillLevel(SkillType.DEFENCE, SkillLevel.SKILL_LEVEL_2),
        DtoMock.skillLevel(SkillType.SPEED, SkillLevel.SKILL_LEVEL_3)
    ]);

    private const last:SkillLevels = new SkillLevels(new <SkillLevelDTO>[
        DtoMock.skillLevel(SkillType.ATTACK, SkillLevel.SKILL_LEVEL_3),
        DtoMock.skillLevel(SkillType.DEFENCE, SkillLevel.SKILL_LEVEL_3),
        DtoMock.skillLevel(SkillType.SPEED, SkillLevel.SKILL_LEVEL_3)
    ]);

    [Test("totalLevel && isLastTotalLevel")]
    public function t0():void {
        assertEquals(0, start.totalLevel);
        assertEquals(1, start.nextTotalLevel);
        assertFalse(start.isLastTotalLevel);

        assertEquals(6, middle.totalLevel);
        assertEquals(7, middle.nextTotalLevel);
        assertFalse(middle.isLastTotalLevel);

        assertEquals(9, last.totalLevel);
        assertTrue(last.isLastTotalLevel);
    }

    [Test("nextTotalLevel invalid", expects="Error")]
    public function t1():void {
        last.nextTotalLevel
    }

    [Test("getLevel")]
    public function t2():void {
        assertEquals(SkillLevel.SKILL_LEVEL_1, middle.getLevel(SkillType.ATTACK));
        assertEquals(SkillLevel.SKILL_LEVEL_2, middle.getLevel(SkillType.DEFENCE));
    }
}
}

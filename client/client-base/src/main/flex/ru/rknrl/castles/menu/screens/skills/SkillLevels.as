package ru.rknrl.castles.menu.screens.skills {
import ru.rknrl.dto.SkillLevel;
import ru.rknrl.dto.SkillLevelDTO;
import ru.rknrl.dto.SkillType;
import ru.rknrl.dto.SkillsDTO;

public class SkillLevels {
    private var skills:SkillsDTO;

    public function SkillLevels(skills:SkillsDTO) {
        this.skills = skills;
    }

    public function getLevel(skillType:SkillType):SkillLevel {
        for each(var level:SkillLevelDTO in skills.levels) {
            if (level.type == skillType) return level.level;
        }
        throw new Error("can't found skill level " + skillType);
    }
}
}

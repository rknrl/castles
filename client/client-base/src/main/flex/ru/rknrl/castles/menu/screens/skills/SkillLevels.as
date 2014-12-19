package ru.rknrl.castles.menu.screens.skills {
import ru.rknrl.dto.SkillLevel;
import ru.rknrl.dto.SkillLevelDTO;
import ru.rknrl.dto.SkillType;
import ru.rknrl.dto.SkillsDTO;

public class SkillLevels {
    private var skills:SkillsDTO;

    public function SkillLevels(skills:SkillsDTO) {
        this.skills = skills;

        var totalLevel:int = 0;
        for each(var level:SkillLevelDTO in skills.levels) {
            totalLevel += level.level.id();
        }
        _totalLevel = totalLevel;
    }

    private var _totalLevel:int;

    public function get totalLevel():int {
        return _totalLevel;
    }

    public function get isLastTotalLevel():Boolean {
        return totalLevel == 9;
    }

    public function getNextTotalLevel():int {
        if (isLastTotalLevel) throw new Error("get nextLevel on lastLevel");
        return totalLevel + 1;
    }

    public function getLevel(skillType:SkillType):SkillLevel {
        for each(var level:SkillLevelDTO in skills.levels) {
            if (level.type == skillType) return level.level;
        }
        throw new Error("can't found skill level " + skillType);
    }
}
}

//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.menu.skills {
import ru.rknrl.dto.SkillLevel;
import ru.rknrl.dto.SkillLevelDTO;
import ru.rknrl.dto.SkillType;

public class SkillLevels {
    private var skills:Vector.<SkillLevelDTO>;

    public function SkillLevels(skills:Vector.<SkillLevelDTO>) {
        this.skills = skills;

        var totalLevel:int = 0;
        for each(var level:SkillLevelDTO in skills) {
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

    public function get nextTotalLevel():int {
        if (isLastTotalLevel) throw new Error("get nextLevel on lastLevel");
        return totalLevel + 1;
    }

    public function getLevel(skillType:SkillType):SkillLevel {
        for each(var level:SkillLevelDTO in skills) {
            if (level.skillType == skillType) return level.level;
        }
        throw new Error("can't found skill level " + skillType);
    }
}
}

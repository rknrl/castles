//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.events {
import flash.events.Event;

import ru.rknrl.dto.SkillLevel;
import ru.rknrl.dto.SkillType;

public class SetSkillEvent extends Event {
    public static const SET_SKILL:String = "setSkill";

    private var _skillType:SkillType;

    public function get skillType():SkillType {
        return _skillType;
    }

    private var _skillLevel:SkillLevel;

    public function get skillLevel():SkillLevel {
        return _skillLevel;
    }

    public function SetSkillEvent(skillType:SkillType, skillLevel:SkillLevel) {
        super(SET_SKILL, true);
        _skillType = skillType;
        _skillLevel = skillLevel;
    }
}
}

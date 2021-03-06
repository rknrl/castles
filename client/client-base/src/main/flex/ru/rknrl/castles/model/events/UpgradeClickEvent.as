//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.events {
import flash.events.Event;

import protos.SkillType;

public class UpgradeClickEvent extends Event {
    public static const UPGRADE_CLICK:String = "upgradeClick";

    private var _skillType:SkillType;

    public function get skillType():SkillType {
        return _skillType;
    }

    public function UpgradeClickEvent(skillType:SkillType) {
        _skillType = skillType;
        super(UPGRADE_CLICK, true);
    }
}
}

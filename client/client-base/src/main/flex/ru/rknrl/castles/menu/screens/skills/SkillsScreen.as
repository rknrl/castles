package ru.rknrl.castles.menu.screens.skills {
import flash.events.Event;
import flash.utils.Dictionary;

import ru.rknrl.castles.menu.screens.Screen;
import ru.rknrl.castles.menu.screens.skills.flask.FlaskView;
import ru.rknrl.castles.rmi.AccountFacadeSender;
import ru.rknrl.castles.utils.Colors;
import ru.rknrl.castles.utils.Utils;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.castles.utils.locale.CastlesLocale;
import ru.rknrl.dto.SkillType;
import ru.rknrl.dto.UpgradeSkillDTO;

public class SkillsScreen extends Screen {
    private var sender:AccountFacadeSender;
    private var locale:CastlesLocale;
    private var skillUpgradePrices:SkillUpgradePrices;

    private const typeToFlask:Dictionary = new Dictionary();

    public function SkillsScreen(skillLevels:SkillLevels, skillUpgradePrices:SkillUpgradePrices, sender:AccountFacadeSender, layout:Layout, locale:CastlesLocale) {
        this.sender = sender;
        this.locale = locale;
        this.skillUpgradePrices = skillUpgradePrices;

        for (var i:int = 0; i < SkillType.values.length; i++) {
            const skillType:SkillType = SkillType.values[i];
            const flask:FlaskView = new FlaskView(skillType, Colors.randomColor(), locale);
            flask.addEventListener(FlaskView.UPGRADE, onUpgrade);
            typeToFlask[skillType] = flask;
            addChild(flask);
        }

        this.skillLevels = skillLevels;
        updateLayout(layout);
    }


    override public function get titleText():String {
        return _skillLevels.isLastTotalLevel ? locale.skillsTitleComplete : locale.skillsTitle + " " + getPrice();
    }

    private function getPrice():int {
        return skillUpgradePrices.getPrice(_skillLevels.getNextTotalLevel());
    }

    public function updateLayout(layout:Layout):void {
        const count:int = SkillType.values.length;
        const top:int = layout.bodyCenterY - FlaskView.HEIGHT / 2;
        const gap:int = layout.shopItemGap;
        const left:int = layout.stageCenterX - ((FlaskView.WIDTH + gap) * count - gap) / 2 + FlaskView.WIDTH / 2;

        for (var i:int = 0; i < count; i++) {
            const skillType:SkillType = SkillType.values[i];

            const flask:FlaskView = typeToFlask[skillType];
            flask.x = left + i * (gap + FlaskView.WIDTH);
            flask.y = top;
        }
    }

    private var _skillLevels:SkillLevels;

    public function set skillLevels(value:SkillLevels):void {
        _skillLevels = value;
        lock = false;

        for each(var skillType:SkillType in SkillType.values) {
            FlaskView(typeToFlask[skillType]).skillLevel = value.getLevel(skillType);
        }
    }

    private function onUpgrade(event:Event):void {
        if (gold < getPrice()) {
            dispatchEvent(new Event(Utils.NOT_ENOUGH_GOLD))
        } else {
            const flask:FlaskView = FlaskView(event.target);
            const dto:UpgradeSkillDTO = new UpgradeSkillDTO();
            dto.type = flask.skillType;
            sender.upgradeSkill(dto);

            lock = true;
            flask.animate();
        }
    }

    private function set lock(value:Boolean):void {
        for each(var flask:FlaskView in typeToFlask) {
            flask.lock = value;
        }
    }
}
}

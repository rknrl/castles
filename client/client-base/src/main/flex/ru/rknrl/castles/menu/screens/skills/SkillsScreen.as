package ru.rknrl.castles.menu.screens.skills {
import flash.display.Sprite;
import flash.events.Event;
import flash.utils.Dictionary;

import ru.rknrl.castles.menu.screens.Screen;
import ru.rknrl.castles.menu.screens.skills.flask.FlaskView;
import ru.rknrl.castles.rmi.AccountFacadeSender;
import ru.rknrl.castles.utils.Align;
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
    private var flasksHolder:Sprite;

    public function SkillsScreen(skillLevels:SkillLevels, skillUpgradePrices:SkillUpgradePrices, sender:AccountFacadeSender, layout:Layout, locale:CastlesLocale) {
        this.sender = sender;
        this.locale = locale;
        this.skillUpgradePrices = skillUpgradePrices;

        addChild(flasksHolder = new Sprite());

        for each(var skillType:SkillType in SkillType.values) {
            const flask:FlaskView = new FlaskView(skillType, Colors.randomColor(), locale);
            flask.addEventListener(FlaskView.UPGRADE, onUpgrade);
            typeToFlask[skillType] = flask;
            flasksHolder.addChild(flask);
        }

        this.skillLevels = skillLevels;
        updateLayout(layout);
    }


    override public function get titleText():String {
        return _skillLevels.isLastTotalLevel ? locale.skillsTitleComplete : locale.skillsTitle + " " + price;
    }

    private function get price():int {
        return skillUpgradePrices.getPrice(_skillLevels.getNextTotalLevel());
    }

    public function updateLayout(layout:Layout):void {
        const flasksWidth:int = Align.horizontal(typeToFlask, FlaskView.WIDTH, layout.shopItemGap);
        flasksHolder.x = layout.stageCenterX - flasksWidth / 2;
        flasksHolder.y = layout.bodyCenterY - FlaskView.HEIGHT / 2
    }

    private var _skillLevels:SkillLevels;

    public function set skillLevels(value:SkillLevels):void {
        _skillLevels = value;

        for each(var skillType:SkillType in SkillType.values) {
            FlaskView(typeToFlask[skillType]).skillLevel = value.getLevel(skillType);
        }

        lock = false;
    }

    private function onUpgrade(event:Event):void {
        if (gold < price) {
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

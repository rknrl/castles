package ru.rknrl.castles.view.menu.skills {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.events.MouseEvent;
import flash.text.TextField;

import ru.rknrl.castles.model.events.UpgradeClickEvent;
import ru.rknrl.castles.model.menu.skills.SkillLevels;
import ru.rknrl.castles.model.menu.skills.SkillUpgradePrices;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.navigate.Screen;
import ru.rknrl.castles.view.utils.Align;
import ru.rknrl.castles.view.utils.applyStarTextFormat;
import ru.rknrl.castles.view.utils.createTextField;
import ru.rknrl.dto.SkillType;

public class SkillsScreen extends Screen {
    private static const flaskWidth:Number = 38;
    private static const flaskGap:Number = 16;

    private var locale:CastlesLocale;

    private var flasksHolder:Sprite;
    private const flasks:Vector.<FlaskView> = new <FlaskView>[];
    private var titleTextField:TextField;

    public function SkillsScreen(skillLevels:SkillLevels, upgradePrices:SkillUpgradePrices, layout:Layout, locale:CastlesLocale) {
        this.locale = locale;
        addChild(flasksHolder = new Sprite());

        for each(var skillType:SkillType in [SkillType.ATTACK, SkillType.SPEED, SkillType.DEFENCE]) {
            const flask:FlaskView = new FlaskView(skillType, skillLevels.getLevel(skillType), locale.skillName(skillType));
            flask.addEventListener(MouseEvent.CLICK, onClick);
            flasks.push(flask);
            flasksHolder.addChild(flask);
        }

        titleTextField = createTextField(Fonts.title);

        _skillLevels = skillLevels;
        _layout = layout;
        this.upgradePrices = upgradePrices;
        this.layout = layout;
    }

    private function getFlask(skillType:SkillType):FlaskView {
        for each(var flask:FlaskView in flasks) {
            if (flask.skillType == skillType) return flask;
        }
        throw new Error("can't find flask " + skillType);
    }

    private var _skillLevels:SkillLevels;

    public function set skillLevels(value:SkillLevels):void {
        _skillLevels = value;
        for each(var skillType:SkillType in SkillType.values) {
            getFlask(skillType).skillLevel = value.getLevel(skillType);
        }
        updateTitleText();
    }

    private var _prices:SkillUpgradePrices;

    public function set upgradePrices(value:SkillUpgradePrices):void {
        _prices = value;
        updateTitleText();
    }

    private function updateTitleText():void {
        titleTextField.text = _skillLevels.isLastTotalLevel ? locale.upgradesComplete : locale.upgradesTitle(_prices.getPrice(_skillLevels.nextTotalLevel));
        applyStarTextFormat(titleTextField);
        alignTitle();
    }

    private var _layout:Layout;

    override public function set layout(value:Layout):void {
        _layout = value;

        flasksHolder.scaleX = flasksHolder.scaleY = value.scale;
        const totalWidth:Number = Align.horizontal(Vector.<DisplayObject>(flasks), flaskWidth, flaskGap) * value.scale;
        flasksHolder.x = value.screenCenterX - totalWidth / 2;
        flasksHolder.y = value.contentCenterY;

        alignTitle();
    }

    private function alignTitle():void {
        titleTextField.scaleX = titleTextField.scaleY = _layout.scale;
        const titlePos:Point = _layout.title(titleTextField.width, titleTextField.height);
        titleTextField.x = titlePos.x;
        titleTextField.y = titlePos.y;
    }

    override public function get titleContent():DisplayObject {
        return titleTextField;
    }

    override public function set lock(value:Boolean):void {
        for each(var flask:FlaskView in flasks) flask.lock = value;
    }

    private function onClick(event:MouseEvent):void {
        const flask:FlaskView = FlaskView(event.target);
        dispatchEvent(new UpgradeClickEvent(flask.skillType));
    }
}
}

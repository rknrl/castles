package ru.rknrl.castles.view.menu.skills {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.events.MouseEvent;

import ru.rknrl.castles.model.events.UpgradeClickEvent;
import ru.rknrl.castles.model.menu.skills.SkillLevels;
import ru.rknrl.castles.model.menu.skills.SkillUpgradePrices;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.navigate.Screen;
import ru.rknrl.castles.view.utils.Align;
import ru.rknrl.castles.view.utils.AnimatedTextField;
import ru.rknrl.dto.SkillType;

public class SkillsScreen extends Screen {
    private var locale:CastlesLocale;

    private var flasksHolder:Sprite;
    private const flasks:Vector.<FlaskView> = new <FlaskView>[];
    private var titleTextField:AnimatedTextField;

    public function SkillsScreen(skillLevels:SkillLevels, upgradePrices:SkillUpgradePrices, layout:Layout, locale:CastlesLocale) {
        this.locale = locale;
        addChild(flasksHolder = new Sprite());

        for each(var skillType:SkillType in [SkillType.ATTACK, SkillType.SPEED, SkillType.DEFENCE]) {
            const flask:FlaskView = new FlaskView(skillType, skillLevels.getLevel(skillType), locale.skillName(skillType));
            flask.addEventListener(MouseEvent.MOUSE_DOWN, onClick);
            flasks.push(flask);
            flasksHolder.addChild(flask);
        }

        titleTextField = new AnimatedTextField(Fonts.title);

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
        alignTitle();
    }

    private var _layout:Layout;

    override public function set layout(value:Layout):void {
        _layout = value;

        flasksHolder.scaleX = flasksHolder.scaleY = value.scale;
        const totalWidth:Number = Align.horizontal(Vector.<DisplayObject>(flasks), Layout.flaskWidth, Layout.flaskGap) * value.scale;
        flasksHolder.x = value.screenCenterX - totalWidth / 2;
        flasksHolder.y = value.contentCenterY;

        alignTitle();
    }

    private function alignTitle():void {
        titleTextField.textScale = _layout.scale;
        const titlePos:Point = _layout.title(titleTextField.textWidth, titleTextField.textHeight);
        titleTextField.x = titlePos.x + titleTextField.textWidth / 2;
        titleTextField.y = titlePos.y + titleTextField.textHeight / 2;
    }

    override public function get titleContent():DisplayObject {
        return titleTextField;
    }

    override public function set lock(value:Boolean):void {
        for each(var flask:FlaskView in flasks) flask.lock = value;
    }

    private function onClick(event:MouseEvent):void {
        event.stopImmediatePropagation();
        const flask:FlaskView = FlaskView(event.target);
        dispatchEvent(new UpgradeClickEvent(flask.skillType));
    }

    public function animate(skillType:SkillType):void {
        getFlask(skillType).bounce();
    }

    override public function animatePrices():void {
        titleTextField.elastic();
    }
}
}

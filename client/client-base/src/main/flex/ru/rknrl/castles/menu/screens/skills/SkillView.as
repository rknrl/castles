package ru.rknrl.castles.menu.screens.skills {

import flash.display.Sprite;
import flash.events.Event;
import flash.events.MouseEvent;

import ru.rknrl.castles.utils.Label;
import ru.rknrl.castles.utils.createTextField;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.castles.utils.locale.CastlesLocale;
import ru.rknrl.dto.SkillLevel;
import ru.rknrl.dto.SkillType;
import ru.rknrl.funnyUi.buttons.round.OkButton;
import ru.rknrl.funnyUi.buttons.round.PlusButton;
import ru.rknrl.utils.changeTextFormat;

public class SkillView extends Sprite {
    public static const UPGRADE:String = "upgrade";

    private var layout:Layout;

    private var _skillType:SkillType;
    public function get skillType():SkillType {
        return _skillType;
    }

    private var nameTextField:Label;
    private var progress:SkillProgress;
    private var plusButton:PlusButton;
    private var okButton:OkButton;

    public function SkillView(skillType:SkillType, width:int, height:int, color:uint, layout:Layout, locale:CastlesLocale) {
        _skillType = skillType;
        addChild(nameTextField = createTextField(layout.skillNameTextFormat, locale.getSkillName(skillType)));
        addChild(progress = new SkillProgress(width, progressHeight(height, nameTextField.height, layout), color, layout));
        addChild(plusButton = new PlusButton(width, color));
        plusButton.addEventListener(MouseEvent.CLICK, onClick);
        addChild(okButton = new OkButton(width, color));

        updateLayout(width, height, layout);
    }

    public function updateLayout(width:int, height:int, layout:Layout):void {
        this.layout = layout;

        changeTextFormat(nameTextField, layout.skillNameTextFormat);
        nameTextField.x = -nameTextField.width / 2;
        nameTextField.y = -height / 2;

        progress.updateLayout(width, progressHeight(height, nameTextField.height, layout), layout);
        progress.y = -height / 2 + nameTextField.height + layout.gap;

        plusButton.updateLayout(width);
        plusButton.y = height / 2 - layout.skillPlusSize / 2;

        okButton.updateLayout(width);
        okButton.y = height / 2 - layout.skillPlusSize / 2;
    }

    private static function progressHeight(height:int, nameTextFieldHeight:int, layout:Layout):int {
        return height - layout.skillPlusSize - layout.gap - layout.gap - nameTextFieldHeight;
    }

    public function set color(value:uint):void {
        nameTextField.textColor = value;
        progress.color = value;
        plusButton.color = value;
        okButton.color = value;
    }

    public function set skillLevel(value:SkillLevel):void {
        progress.skillLevel = value;
        plusButton.visible = value != SkillLevel.SKILL_LEVEL_3;
        okButton.visible = value == SkillLevel.SKILL_LEVEL_3;
    }

    private function onClick(event:MouseEvent):void {
        dispatchEvent(new Event(UPGRADE));
    }

    public function animate():void {
        plusButton.playBounce();
        okButton.playBounce();
    }

    public function lock():void {
        plusButton.lock();
    }

    public function unlock():void {
        plusButton.unlock();
    }
}
}

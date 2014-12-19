package ru.rknrl.castles.menu.screens.skills {

import flash.display.Shape;
import flash.display.Sprite;
import flash.events.Event;
import flash.events.MouseEvent;

import ru.rknrl.castles.utils.Colors;
import ru.rknrl.castles.utils.Label;
import ru.rknrl.castles.utils.createTextField;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.castles.utils.locale.CastlesLocale;
import ru.rknrl.dto.SkillLevel;
import ru.rknrl.dto.SkillType;
import ru.rknrl.utils.changeTextFormat;

public class FlaskView extends Sprite {
    public static const UPGRADE:String = "upgrade";

    private var layout:Layout;

    private var _skillType:SkillType;

    public function get skillType():SkillType {
        return _skillType;
    }

    private var nameTextField:Label;
    private var fill:FlaskFill;

    private var flask:Flask;
    private var flaskMarks:FlaskMarks;
    private var shadow:Shape;

    const fillWidth:int = 34; // todo
    const fillHeight:int = 93; // todo

    public function FlaskView(skillType:SkillType, width:int, height:int, color:uint, layout:Layout, locale:CastlesLocale) {
        _skillType = skillType;

        addChild(nameTextField = createTextField(layout.skillNameTextFormat, locale.getSkillName(skillType)));

        flask = new Flask();
        flask.transform.colorTransform = Colors.colorToTransform(color);
        addChild(flask);

        fill = new FlaskFill(fillWidth, fillHeight, color, layout);
        fill.transform.colorTransform = Colors.colorToTransform(color);
        addChild(fill);

        flaskMarks = new FlaskMarks();
        addChild(flaskMarks);

        shadow = new Shape();
        shadow.graphics.beginFill(0x444444, 0.2);
        shadow.graphics.drawEllipse(-49 / 4, 0, 49 / 2, 6 / 2);
        shadow.graphics.endFill();
        addChild(shadow);

        addEventListener(MouseEvent.CLICK, onClick);
        updateLayout(width, height, layout);
        this.color = color;
    }

    public function updateLayout(width:int, height:int, layout:Layout):void {
        this.layout = layout;

        changeTextFormat(nameTextField, layout.skillNameTextFormat);
        nameTextField.x = -nameTextField.width / 2;
        nameTextField.y = 0;

        const flaskY:int = 60;

        flask.x = 0;
        flask.y = flaskY;

        fill.updateLayout(fillWidth, fillHeight, layout);
        fill.y = flaskY;

        flaskMarks.x = 0;
        flaskMarks.y = flaskY;

        shadow.x = 0;
        shadow.y = height; // todo
    }


    public function set color(value:uint):void {
        nameTextField.textColor = value;
        fill.color = value;
    }

    private var _skillLevel:SkillLevel;

    public function set skillLevel(value:SkillLevel):void {
        _skillLevel = value;
        fill.skillLevel = value;
    }

    private function onClick(event:MouseEvent):void {
        if (_skillLevel != SkillLevel.SKILL_LEVEL_3) {
            dispatchEvent(new Event(UPGRADE));
        }
    }

    public function animate():void {
//        plusButton.playBounce();
//        okButton.playBounce();
    }

    public function lock():void {
//        plusButton.lock();
    }

    public function unlock():void {
//        plusButton.unlock();
    }
}
}

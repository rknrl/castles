package ru.rknrl.castles.menu.screens.skills.flask {

import flash.display.Sprite;
import flash.events.Event;
import flash.events.MouseEvent;

import ru.rknrl.castles.utils.Colors;
import ru.rknrl.castles.utils.Label;
import ru.rknrl.castles.utils.Shadow;
import ru.rknrl.castles.utils.animation.Fly;
import ru.rknrl.castles.utils.createTextField;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.castles.utils.locale.CastlesLocale;
import ru.rknrl.dto.SkillLevel;
import ru.rknrl.dto.SkillType;

public class FlaskView extends Sprite {
    public static const UPGRADE:String = "upgrade";

    public static const WIDTH:int = 75;
    public static const HEIGHT:int = 182;
    private static const flaskY:int = 50;

    private var _skillType:SkillType;

    public function get skillType():SkillType {
        return _skillType;
    }

    private var fill:FlaskFill;
    private var fly:Fly;

    public function FlaskView(skillType:SkillType, color:uint, locale:CastlesLocale) {
        _skillType = skillType;

        const nameTextField:Label = createTextField(Layout.skillNameTextFormat, locale.getSkillName(skillType))
        nameTextField.x = -nameTextField.width / 2;
        nameTextField.textColor = color;
        addChild(nameTextField);

        const flaskHolder:Sprite = new Sprite();
        flaskHolder.y = flaskY;
        addChild(flaskHolder);

        const flask:Flask = new Flask();
        flaskHolder.addChild(flask);
        flask.transform.colorTransform = Colors.colorToTransform(color);

        flaskHolder.addChild(fill = new FlaskFill());
        fill.transform.colorTransform = Colors.colorToTransform(color);

        const flaskMarks:FlaskMarks = new FlaskMarks();
        flaskHolder.addChild(flaskMarks);

        const shadow:Shadow = new Shadow();
        shadow.y = HEIGHT;
        addChild(shadow);

        fly = new Fly(flaskHolder, shadow);

        addEventListener(MouseEvent.CLICK, onClick);
        addEventListener(Event.ENTER_FRAME, onEnterFrame);
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

    public function set lock(value:Boolean):void {
//        plusButton.lock();
    }

    private function onEnterFrame(event:Event):void {
        fly.onEnterFrame();
        fill.onEnterFrame(fly.fraction);
    }
}
}

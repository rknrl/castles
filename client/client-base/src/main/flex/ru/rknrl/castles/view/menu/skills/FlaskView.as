package ru.rknrl.castles.view.menu.skills {
import flash.display.Bitmap;
import flash.display.Sprite;
import flash.events.Event;
import flash.text.TextField;

import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.utils.Animated;
import ru.rknrl.castles.view.utils.Fly;
import ru.rknrl.castles.view.utils.LockView;
import ru.rknrl.castles.view.utils.Shadow;
import ru.rknrl.castles.view.utils.createTextField;
import ru.rknrl.dto.SkillLevel;
import ru.rknrl.dto.SkillType;

public class FlaskView extends Animated {
    private static const flaskHeight:Number = 104;
    private static const textFlaskGap:Number = 16;
    private static const textHeight:Number = 22;
    private static const textY:Number = -flaskHeight / 2 - textFlaskGap - textHeight;

    private static const mouseHolderW:Number = 38 + 16;
    private static const mouseHolderH:Number = 200;

    private static function skillTypeToTextFieldX(skillType:SkillType, width:Number):Number {
        switch (skillType) {
            case SkillType.ATTACK:
                return -width + 12;
            case SkillType.SPEED:
                return -width / 2;
            case SkillType.DEFENCE:
                return -12;
        }
        throw new Error("unknown skillType " + skillType);
    }

    private var fly:Fly;

    private var fill:FlaskFill;
    private var lockView:LockView;

    public function FlaskView(skillType:SkillType, skillLevel:SkillLevel, name:String) {
        _skillType = skillType;

        mouseChildren = false;

        const mouseHolder:Bitmap = new Bitmap(Colors.transparent);
        mouseHolder.width = mouseHolderW;
        mouseHolder.height = mouseHolderH;
        mouseHolder.x = -mouseHolderW / 2;
        mouseHolder.y = -mouseHolderH / 2;
        addChild(mouseHolder);

        const textField:TextField = createTextField(Fonts.skillName);
        textField.textColor = Colors.skill(skillType);
        textField.text = name;
        textField.x = skillTypeToTextFieldX(skillType, textField.width);
        textField.y = textY;
        addChild(textField);

        const flaskHolder:Sprite = new Sprite();
        addChild(flaskHolder);

        const flask:Flask = new Flask();
        flask.transform.colorTransform = Colors.transform(Colors.skill(skillType));
        flaskHolder.addChild(flask);

        flaskHolder.addChild(fill = new FlaskFill(skillLevel));
        fill.transform.colorTransform = Colors.transform(Colors.skill(skillType));

        flaskHolder.addChild(lockView = new LockView());

        const shadow:Shadow = new Shadow();
        shadow.y = flaskHeight / 2 + Layout.shadowDistance;
        addChild(shadow);

        fly = new Fly(flaskHolder, shadow);

        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    private var _skillType:SkillType;

    public function get skillType():SkillType {
        return _skillType;
    }

    public function set skillLevel(value:SkillLevel):void {
        fill.skillLevel = value;
    }

    private function onEnterFrame(event:Event):void {
        fly.onEnterFrame();
        fill.onEnterFrame(fly.fraction);
    }

    public function set lock(value:Boolean):void {
        lockView.visible = value;
        mouseEnabled = !value;
    }
}
}

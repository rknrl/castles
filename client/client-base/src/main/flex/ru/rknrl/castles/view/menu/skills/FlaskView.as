package ru.rknrl.castles.view.menu.skills {
import flash.display.Bitmap;
import flash.display.Sprite;
import flash.events.Event;
import flash.text.TextField;

import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.utils.Animated;
import ru.rknrl.castles.view.utils.AnimatedShadow;
import ru.rknrl.castles.view.utils.Fly;
import ru.rknrl.castles.view.utils.LockView;
import ru.rknrl.dto.SkillLevel;
import ru.rknrl.dto.SkillType;
import ru.rknrl.utils.createTextField;

public class FlaskView extends Sprite {
    private static const flaskHeight:Number = 104;
    private static const textFlaskGap:Number = 16;
    private static const textHeight:Number = 22;
    private static const textY:Number = -flaskHeight / 2 - textFlaskGap - textHeight;

    private static const mouseHolderW:Number = Layout.flaskWidth + Layout.flaskGap;
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

    private var flaskHolder:Animated;
    private var fill:FlaskFill;
    private var lockView:LockView;
    private var shadow:AnimatedShadow;

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

        addChild(flaskHolder = new Animated());

        const flask:FlaskMC = new FlaskMC();
        flask.transform.colorTransform = Colors.transform(Colors.skill(skillType));
        flaskHolder.addChild(flask);

        flaskHolder.addChild(fill = new FlaskFill(skillLevel));
        fill.transform.colorTransform = Colors.transform(Colors.skill(skillType));

        flaskHolder.addChild(lockView = new LockView());

        shadow = new AnimatedShadow();
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

    public function bounce():void {
        flaskHolder.bounce();
        shadow.bounce();
    }
}
}

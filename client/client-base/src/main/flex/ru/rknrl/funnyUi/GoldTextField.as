package ru.rknrl.funnyUi {
import flash.display.Sprite;
import flash.text.TextFormat;

import ru.rknrl.castles.utils.Label;
import ru.rknrl.castles.utils.createTextField;
import ru.rknrl.utils.changeTextFormat;

public class GoldTextField extends Sprite {
    private static const STAR:String = "★";

    private var textField:Label;
    private var goldHolder:Animated;
    private var goldTextField:Label;

    public function GoldTextField(text:String, textFormat:TextFormat, gold:int, color:uint) {
        mouseChildren = false;

        addChild(textField = createTextField(textFormat, text));

        goldHolder = new Animated();
        goldHolder.x = textField.width;
        addChild(goldHolder);

        goldHolder.addChild(goldTextField = createTextField(textFormat, gold + STAR));

        this.color = color;
    }

    public function set textFormat(textFormat:TextFormat):void {
        changeTextFormat(textField, textFormat);
        changeTextFormat(goldTextField, textFormat);

        goldHolder.x = textField.width;
    }

    public function set gold(value:int):void {
        goldTextField.text = value + STAR;
        goldHolder.playBounce()
    }

    public function animate():void {
        goldHolder.playElastic();
    }

    public function set color(value:uint):void {
        textField.textColor = value;
        goldTextField.textColor = value;
    }
}
}
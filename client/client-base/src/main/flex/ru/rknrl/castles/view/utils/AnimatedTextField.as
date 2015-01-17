package ru.rknrl.castles.view.utils {
import flash.text.TextField;
import flash.text.TextFormat;

import ru.rknrl.utils.centerize;

public class AnimatedTextField extends Animated {
    private var textField:TextField;

    public function AnimatedTextField(textFormat:TextFormat) {
        addChild(textField = createTextField(textFormat));
    }

    public function set text(value:String):void {
        textField.text = value;
        applyStarTextFormat(textField);
        centerize(textField);
    }

    public function set textScale(value: Number):void {
        textField.scaleX = textField.scaleY = value;
        centerize(textField);
    }

    public function get text():String {
        return textField.text;
    }

    public function get textWidth():Number {
        return textField.width;
    }

    public function get textHeight():Number {
        return textField.height;
    }
}
}
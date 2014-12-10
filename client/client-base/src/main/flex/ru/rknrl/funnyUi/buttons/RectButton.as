package ru.rknrl.funnyUi.buttons {
import flash.text.TextFormat;

import ru.rknrl.castles.utils.Label;
import ru.rknrl.castles.utils.createTextField;
import ru.rknrl.funnyUi.Animated;
import ru.rknrl.funnyUi.Lock;
import ru.rknrl.utils.centerize;
import ru.rknrl.utils.changeTextFormat;

public class RectButton extends Animated {
    protected var textField:Label;
    private var lockView:Lock;
    private var w:int;
    private var h:int;
    private var corner:int;

    public function RectButton(w:int, h:int, corner:int, text:String, textFormat:TextFormat, color:uint) {
        mouseChildren = false;

        addChild(textField = createTextField(textFormat, text));
        addChild(lockView = new Lock());
        lockView.visible = false;

        _color = color;

        updateLayout(w, h, corner, textFormat);
    }

    public function updateLayout(w:int, h:int, corner:int, textFormat:TextFormat):void {
        this.w = w;
        this.h = h;
        this.corner = corner;
        redrawRect();

        lockView.width = lockView.height = h / 2;

        changeTextFormat(textField, textFormat);
        centerize(textField);
    }

    private function redrawRect():void {
        graphics.clear();
        graphics.beginFill(_color);
        graphics.drawRoundRect(-w / 2, -h / 2, w, h, corner, corner);
        graphics.endFill();
    }

    public function set text(value:String):void {
        textField.text = value;
        centerize(textField);
    }

    private var _color:uint;

    public function set color(value:uint):void {
        _color = value;
        redrawRect();
    }

    public function lock():void {
        lockView.visible = true;
        textField.visible = false;
        mouseEnabled = false;
    }

    public function unlock():void {
        lockView.visible = false;
        textField.visible = true;
        mouseEnabled = true;
    }
}
}

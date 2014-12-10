package ru.rknrl.castles.menu.screens.bank {
import flash.display.Sprite;
import flash.text.TextFormat;

import ru.rknrl.castles.utils.Label;
import ru.rknrl.castles.utils.createTextField;
import ru.rknrl.utils.centerize;
import ru.rknrl.utils.changeTextFormat;
import ru.rknrl.utils.drawCircle;

public class Circle extends Sprite {
    private var textField:Label;

    public function Circle(text:String, textFormat:TextFormat, radius:int, color:uint) {
        mouseChildren = false;

        addChild(textField = createTextField(textFormat, text));

        _color = color;
        updateLayout(textFormat, radius);
    }

    private var _radius:int;

    public function updateLayout(textFormat:TextFormat, radius:int):void {
        _radius = radius;
        redrawCircle();

        changeTextFormat(textField, textFormat);
        centerize(textField);
    }

    private var _color:uint;

    public function set color(value:uint):void {
        _color = value;
        redrawCircle();
    }

    public function redrawCircle():void {
        graphics.clear();
        drawCircle(graphics, _radius, _color);
    }
}
}

package ru.rknrl.funnyUi.buttons.round {
import flash.display.CapsStyle;
import flash.display.Graphics;
import flash.display.LineScaleMode;
import flash.display.Shape;

import ru.rknrl.funnyUi.Animated;

public class OkButton extends Animated {
    private static const originalRadius:int = 32;

    private var icon:Shape;
    private var radius:int;

    public function OkButton(radius:int, color:uint) {
        this.radius = radius;
        _color = color;

        addChild(icon = new Shape());

        updateLayout(radius)
    }

    public function updateLayout(radius:int):void {
        this.radius = radius;
        redraw();
        const ratio:Number = radius / originalRadius;
        icon.scaleX = icon.scaleY = ratio;
    }

    private function drawCircle(radius:int, color:uint):void {
        graphics.lineStyle(radius / 5, color, 1, false, LineScaleMode.NORMAL, CapsStyle.NONE);
        graphics.drawCircle(0, 0, radius);
    }

    private static function drawIcon(g:Graphics, color:uint):void {
        g.lineStyle(10, color, 1, false, LineScaleMode.NORMAL, CapsStyle.NONE);
        g.moveTo(-10 - 5, -10 + 10);
        g.lineTo(-5, 10);
        g.lineTo(20 - 5, -20 + 10);
    }

    private var _color:uint;

    public function set color(value:uint):void {
        _color = value;
        redraw();
    }

    private function redraw():void {
        graphics.clear();
        drawCircle(radius, _color);

        icon.graphics.clear();
        drawIcon(icon.graphics, _color)
    }
}
}

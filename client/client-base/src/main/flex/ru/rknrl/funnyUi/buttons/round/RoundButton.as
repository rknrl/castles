package ru.rknrl.funnyUi.buttons.round {
import flash.display.Shape;

import ru.rknrl.funnyUi.Animated;
import ru.rknrl.funnyUi.Lock;
import ru.rknrl.utils.OverrideMe;
import ru.rknrl.utils.drawCircle;

public class RoundButton extends Animated {
    private static const originalRadius:int = 32;

    protected var icon:Shape;
    private var lockView:Lock;
    private var radius:int;

    public function RoundButton(radius:int, color:uint) {
        this.radius = radius;
        _color = color;

        addChild(icon = createIcon());

        addChild(lockView = new Lock());
        lockView.visible = false;

        updateLayout(radius);
    }

    protected function createIcon():Shape {
        throw OverrideMe();
    }

    private var _color:uint;

    public function set color(value:uint):void {
        _color = value;
        redrawCircle();
    }

    private function redrawCircle():void {
        graphics.clear();
        drawCircle(graphics, radius, _color)
    }

    public function updateLayout(radius:int):void {
        this.radius = radius;
        redrawCircle();

        const ratio:Number = radius / originalRadius;
        icon.scaleX = icon.scaleY = ratio;

        lockView.scaleX = lockView.scaleY = ratio;
    }

    public function lock():void {
        lockView.visible = true;
        icon.visible = false;
        mouseEnabled = false;
        mouseChildren = false;
    }

    public function unlock():void {
        lockView.visible = false;
        icon.visible = true;
        mouseEnabled = true;
        mouseChildren = true;
    }
}
}

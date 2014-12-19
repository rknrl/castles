package ru.rknrl.castles.utils.animation {
import flash.display.DisplayObject;
import flash.utils.getTimer;

public class Fly {
    private static const deltaY:int = 4;

    private var displayObject:DisplayObject;
    private var displayObjectY:Number;
    private var shadow:DisplayObject;
    private var rnd:Number;

    public function Fly(displayObject:DisplayObject, shadow:DisplayObject) {
        this.displayObject = displayObject;
        this.displayObjectY = displayObject.y;
        this.shadow = shadow;
        rnd = Math.random();
    }

    private var _fraction:Number;

    /**
     * from -1 to 1
     */
    public function get fraction():Number {
        return _fraction;
    }

    public function onEnterFrame():void {
        _fraction = Math.sin(getTimer() / 300 + rnd);
        displayObject.y = displayObjectY + _fraction * deltaY;

        const unit:Number = (_fraction + 1) / 2; // from 0 to 1
        const scale:Number = 0.5 + unit * 0.5;
        shadow.scaleX = shadow.scaleY = scale;
    }
}
}

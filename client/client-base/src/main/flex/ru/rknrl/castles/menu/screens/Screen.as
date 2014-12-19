package ru.rknrl.castles.menu.screens {
import flash.display.Sprite;
import flash.events.Event;
import flash.utils.getTimer;

import ru.rknrl.easers.Bounce;
import ru.rknrl.easers.IEaser;
import ru.rknrl.easers.interpolate;

public class Screen extends Sprite {
    public function Screen() {
        addEventListener(Event.ENTER_FRAME, onEnterFrame);
        startTime = getTimer();
        inTransition = 0;
    }

    public function get titleText():String {
        return "";
    }

    private var _gold:int;

    public function get gold():int {
        return _gold;
    }

    public function set gold(value:int):void {
        _gold = value;
    }

    private var startTime:int;
    private static const duration:int = 1000;
    private static const easer:IEaser = new Bounce();

    private function onEnterFrame(event:Event):void {
        inTransition = interpolate(0, 1, getTimer(), startTime, duration, easer)
    }

    protected function set inTransition(value:Number):void {
        // override me
    }
}
}

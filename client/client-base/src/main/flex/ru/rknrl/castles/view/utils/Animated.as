package ru.rknrl.castles.view.utils {
import flash.display.Sprite;
import flash.events.Event;
import flash.utils.getTimer;

import ru.rknrl.easers.Bounce;
import ru.rknrl.easers.Elastic;
import ru.rknrl.easers.IEaser;
import ru.rknrl.easers.interpolate;

public class Animated extends Sprite {
    private static const bounceEaser:IEaser = new Bounce();
    private static const elasticEaser:IEaser = new Elastic();
    private static const duration:int = 500;

    private var startTime:int;
    private var easer:IEaser = bounceEaser;

    public function Animated() {
        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    public function bounce():void {
        easer = bounceEaser;
        startTime = getTimer();
    }

    public function elastic():void {
        easer = elasticEaser;
        startTime = getTimer();
    }

    private function onEnterFrame(event:Event):void {
        scaleX = scaleY = interpolate(0.5, 1, getTimer(), startTime, duration, easer);
    }
}
}

package ru.rknrl.funnyUi {
import flash.display.Sprite;
import flash.events.Event;
import flash.utils.getTimer;

import ru.rknrl.easers.Bounce;
import ru.rknrl.easers.Elastic;
import ru.rknrl.easers.IEaser;
import ru.rknrl.easers.interpolate;

public class Animated extends Sprite {
    private static const bounce:IEaser = new Bounce();
    private static const bounceDuration:int = 500;

    private static const elastic:IEaser = new Elastic();
    private static const elasticDuration:int = 1500;

    private var easer:IEaser;
    private var duration:int;
    private var startTime:int;

    public function Animated() {
        addEventListener(Event.ENTER_FRAME, onEnterFrame)
    }

    public function playBounce():void {
        easer = bounce;
        duration = bounceDuration;
        startTime = getTimer();
    }

    public function playElastic():void {
        easer = elastic;
        duration = elasticDuration;
        startTime = getTimer();
    }

    private function onEnterFrame(event:Event):void {
        if (easer) {
            const progress:Number = interpolate(0, 1, getTimer(), startTime, duration, easer);
            scaleX = scaleY = 0.5 + progress * 0.5;
            if (getTimer() - startTime > duration) {
                easer = null;
            }
        }
    }
}
}

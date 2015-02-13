//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

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
    private static const bounceDuration:int = 500;
    private static const elasticDuration:int = 1500;

    private var startTime:int;
    private var easer:IEaser = bounceEaser;
    private var duration:int = bounceDuration;

    public function Animated() {
        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    public function bounce():void {
        easer = bounceEaser;
        duration = bounceDuration;
        startTime = getTimer();
    }

    public function elastic():void {
        easer = elasticEaser;
        duration = elasticDuration;
        startTime = getTimer();
    }

    private function onEnterFrame(event:Event):void {
        scaleX = scaleY = interpolate(0.5, 1, getTimer(), startTime, duration, easer);
    }
}
}

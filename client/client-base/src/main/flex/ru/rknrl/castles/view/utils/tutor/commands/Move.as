//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.utils.tutor.commands {
import flash.display.DisplayObject;
import flash.events.Event;
import flash.events.EventDispatcher;
import flash.utils.getTimer;

import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.model.points.Points;
import ru.rknrl.easers.IEaser;
import ru.rknrl.easers.Linear;
import ru.rknrl.easers.interpolate;

public class Move extends EventDispatcher implements ITutorCommand {
    private static const easer:IEaser = new Linear(0, 1);
    private var startTime:int;
    private var duration:int;
    private var points:Points;
    private var target:DisplayObject;

    public function Move(points:Points, target:DisplayObject, duration:int) {
        this.points = points;
        this.target = target;
        this.duration = duration;
    }

    public function execute():void {
        startTime = getTimer();
    }

    public function enterFrame():void {
        const time:int = getTimer();
        const progress:Number = interpolate(0, 1, time, startTime, duration, easer);
        const pos:Point = points.getPos(points.totalDistance * progress);
        target.x = pos.x;
        target.y = pos.y;
        if (progress == 1) dispatchEvent(new Event(Event.COMPLETE));
    }
}
}

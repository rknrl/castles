//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.utils.tutor {
import flash.display.Shape;
import flash.display.Sprite;
import flash.events.Event;
import flash.utils.getTimer;

import ru.rknrl.easers.IEaser;
import ru.rknrl.easers.Linear;
import ru.rknrl.easers.interpolate;

public class CursorHalo extends Sprite {
    private static const MODE_NONE:int = 0;
    private static const MODE_MOUSE_DOWN:int = 1;
    private static const MODE_MOUSE_UP:int = 2;

    private static const radius:int = 48;
    private static const easer:IEaser = new Linear(0, 1);
    private static const duration:int = 500;

    private static const startScale:Number = 0.8;
    private static const endScale:Number = 1;

    private static const startAlpha:Number = 0.3;
    private static const endAlpha:Number = 0.5;

    private static const borderStartScale:Number = 0.5;
    private static const borderEndScale:Number = 2;

    private static const borderStartAlpha:Number = 1;
    private static const borderEndAlpha:Number = 0;

    private var mode:int;
    private var circle:Shape;
    private var border:Shape;
    private var startTime:int;

    public function CursorHalo() {
        circle = new Shape();
        circle.graphics.beginFill(0xffffff, 0.5);
        circle.graphics.drawCircle(0, 0, radius);
        addChild(circle);

        border = new Shape();
        border.graphics.lineStyle(10, 0xffffff);
        border.graphics.drawCircle(0, 0, radius);
        addChild(border);

        clear();

        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    public function mouseDown():void {
        play(MODE_MOUSE_DOWN);
    }

    public function mouseUp():void {
        play(MODE_MOUSE_UP);
    }

    public function clear():void {
        circle.alpha = border.alpha = 0;
        startTime = 0;
        mode = MODE_NONE;
    }

    private function play(mode:int):void {
        this.mode = mode;
        startTime = getTimer();
        onEnterFrame();
    }

    private function onEnterFrame(event:Event = null):void {
        const time:int = getTimer();

        if (mode == MODE_MOUSE_DOWN) {
            border.alpha = 0;

            circle.scaleX = circle.scaleY = interpolate(startScale, endScale, time, startTime, duration, easer);
            circle.alpha = interpolate(startAlpha, endAlpha, time, startTime, duration, easer);
        } else if (mode == MODE_MOUSE_UP) {
            circle.alpha = interpolate(endAlpha, 0, time, startTime, duration, easer);

            border.scaleX = border.scaleY = interpolate(borderStartScale, borderEndScale, time, startTime, duration, easer);
            border.alpha = interpolate(borderStartAlpha, borderEndAlpha, time, startTime, duration, easer);
        }
    }
}
}

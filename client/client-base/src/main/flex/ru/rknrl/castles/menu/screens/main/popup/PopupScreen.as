package ru.rknrl.castles.menu.screens.main.popup {
import flash.display.Bitmap;
import flash.display.Sprite;
import flash.events.Event;
import flash.utils.getTimer;

import ru.rknrl.castles.utils.Utils;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.easers.interpolate;

public class PopupScreen extends Sprite {
    private var bitmap:Bitmap;
    private var startAlpha:Number;
    private var endAlpha:Number;
    private var startTime:int;
    private var duration:int;
    private var moving:Boolean;

    public function PopupScreen(layout:Layout) {
        addChild(bitmap = new Bitmap(Utils.popupScreen));

        open();

        updateLayout(layout);
        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    public function updateLayout(layout:Layout):void {
        bitmap.width = layout.stageWidth;
        bitmap.height = layout.stageHeight;
    }

    private function open():void {
        startAlpha = 0;
        alpha = startAlpha;
        endAlpha = 1;
        startTime = getTimer();
        duration = Utils.popupDuration;
        moving = true;
    }

    public function close():void {
        startAlpha = alpha;
        endAlpha = 0;
        startTime = getTimer();
        duration = Utils.popupDuration * alpha;
        moving = true;
    }

    private function onEnterFrame(event:Event):void {
        if (moving) {
            alpha = interpolate(startAlpha, endAlpha, getTimer(), startTime, duration, Utils.popupEaser);
            if (alpha == endAlpha) {
                dispatchEvent(new Event(Event.COMPLETE));
                moving = false;
            }
        }
    }
}
}

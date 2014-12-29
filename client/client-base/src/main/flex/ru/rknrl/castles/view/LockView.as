package ru.rknrl.castles.view {
import flash.display.Bitmap;
import flash.display.BitmapData;
import flash.display.Sprite;
import flash.events.Event;
import flash.utils.getTimer;

public class LockView extends Sprite {
    private var bar:Sprite;

    public function LockView() {
        addChild(bar = new Sprite());
        const bitmap:Bitmap = new Bitmap(new BitmapData(32, 32, false, 0xffffff));
        bitmap.x = -32 / 2;
        bitmap.y = -32 / 2;
        bar.addChild(bitmap);
        addEventListener(Event.ENTER_FRAME, onEnterFrame);
        visible = false;
    }

    private function onEnterFrame(event:Event):void {
        bar.rotation = getTimer();
    }
}
}

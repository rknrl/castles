package ru.rknrl.funnyUi {
import flash.display.Bitmap;
import flash.display.BitmapData;
import flash.display.Sprite;
import flash.events.Event;
import flash.utils.getTimer;

public class Lock extends Sprite {
    private var holder:Sprite;
    private var bitmap:Bitmap;

    public function Lock() {
        addChild(holder = new Sprite());

        holder.addChild(bitmap = new Bitmap(new BitmapData(32, 32, false, 0xffffff)));
        bitmap.x = -16;
        bitmap.y = -16;

        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    private function onEnterFrame(event:Event):void {
        holder.rotation = getTimer() / 4;
    }
}
}

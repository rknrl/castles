package ru.rknrl.castles.menu.screens.skills {
import flash.display.Bitmap;
import flash.display.BitmapData;
import flash.display.Sprite;
import flash.events.Event;
import flash.utils.getTimer;

public class FlaskWaterLine extends Sprite {
    private var bitmap:Bitmap;
    private var maxH:int;
    private var startTime: int;

    public function FlaskWaterLine(w:int, maxH:int) {
        this.maxH = maxH;
        addChild(bitmap = new Bitmap(new BitmapData(1, 1, false, 0xcccccc)));
        bitmap.width = w;
        bitmap.x = -w / 2;
        startTime = Math.random() * 1000;
        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    private function onEnterFrame(event:Event):void {
        const height:int = Math.abs(Math.sin((getTimer() - startTime) / 300)) * maxH;
        bitmap.height = height;
        bitmap.y = -height;
    }
}
}

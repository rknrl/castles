package ru.rknrl.castles.view.game.area.volcanoes {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.events.Event;
import flash.utils.getTimer;

import ru.rknrl.castles.utils.points.Point;

public class VolcanoView extends Sprite {
    private var volcano:DisplayObject;

    public function VolcanoView(pos:Point, radius:Number) {
        volcano = new VolcanoMC();
        volcano.x = pos.x;
        volcano.y = pos.y;
        addChild(volcano);
        this.radius = radius;
        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    public function set radius(value:Number):void {
        volcano.width = volcano.height = value;
    }

    private function onEnterFrame(event:Event):void {
        scaleX = scaleY = 1 + Math.sin(getTimer() / 100) * 0.1;
    }
}
}

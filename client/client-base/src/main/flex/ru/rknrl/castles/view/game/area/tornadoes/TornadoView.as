package ru.rknrl.castles.view.game.area.tornadoes {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.events.Event;
import flash.utils.getTimer;

import ru.rknrl.castles.utils.points.Point;

public class TornadoView extends Sprite {
    private var tornado:DisplayObject;

    public function TornadoView(pos:Point) {
        addChild(tornado = new TornadoMC());
        this.pos = pos;
        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    public function set pos(value:Point):void {
        tornado.x = value.x;
        tornado.y = value.y;
    }

    private function onEnterFrame(event:Event):void {
        rotation = Math.sin(getTimer() / 100) * 20;
    }
}
}

package ru.rknrl.castles.game.view.items {
import flash.display.Sprite;
import flash.geom.Point;

import ru.rknrl.Points;

public class TornadoView extends Sprite {
    private static const w:Number = 20;
    private static const h:Number = 20;

    private var _startTime:int;

    public function get startTime():int {
        return _startTime;
    }

    private var _millisFromStart:int;

    public function get millisFromStart():int {
        return _millisFromStart;
    }

    private var _millisTillEnd:int;

    public function get millisTillEnd():int {
        return _millisTillEnd;
    }

    private var points:Points;

    private var speed:Number;

    public function TornadoView(startTime:int, millisFromStart:int, millisTillEnd:int, points:Points, speed:Number) {
        _startTime = startTime;
        _millisFromStart = millisFromStart;
        _millisTillEnd = millisTillEnd;
        this.points = points;
        this.speed = speed;

        const color:uint = 0x2ad9dc;
        graphics.beginFill(color);
        graphics.moveTo(-w / 2, -h);
        graphics.lineTo(w / 2, -h);
        graphics.lineTo(0, 0);
        graphics.endFill();
    }

    public function getPos(time:int):Point {
        const currentTime:int = time - startTime + millisFromStart;
        return points.getPos(currentTime * speed);
    }

    public function update(time:int):void {
        rotation = Math.sin(time / 100) * 20;
    }
}
}

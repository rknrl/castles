package ru.rknrl.castles.game.view.items {
import flash.display.Sprite;
import flash.geom.Point;

import ru.rknrl.utils.drawCircle;

public class BulletView extends Sprite {
    private static const radius:Number = 3;

    private var startPos:Point;
    private var endPos:Point;

    private var _startTime:int;

    public function get startTime():int {
        return _startTime;
    }

    private var _duration:int;

    public function get duration():int {
        return _duration;
    }

    private var dx:Number;
    private var dy:Number;

    public function BulletView(startPos:Point, endPos:Point, startTime:int, duration:int) {
        this.startPos = startPos;
        this.endPos = endPos;
        _startTime = startTime;
        _duration = duration;

        dx = endPos.x - startPos.x;
        dy = endPos.y - startPos.y;

        drawCircle(graphics, radius, 0x555555);
    }

    public function getPos(time:int):Point {
        const progress:Number = (time - startTime) / _duration;
        return new Point(startPos.x + dx * progress, startPos.y + dy * progress);
    }
}
}

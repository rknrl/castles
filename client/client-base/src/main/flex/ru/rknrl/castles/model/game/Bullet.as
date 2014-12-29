package ru.rknrl.castles.model.game {
import ru.rknrl.castles.utils.points.Point;

public class Bullet {
    private var startPos:Point;
    private var endPos:Point;
    private var startTime:int;
    private var duration:int;

    public function Bullet(id:int, startPos:Point, endPos:Point, startTime:int, duration:int) {
        _id = id;
        this.startPos = startPos;
        this.endPos = endPos;
        this.startTime = startTime;
        this.duration = duration;
    }

    private var _id:int;

    public function get id():int {
        return _id;
    }

    public function pos(time:int):Point {
        const progress:Number = (time - startTime) / duration;
        return startPos.lerp(endPos, progress);
    }

    public function needRemove(time:int):Boolean {
        return time - startTime > duration;
    }
}
}

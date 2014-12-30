package ru.rknrl.castles.model.game {
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.model.points.Points;

public class Tornado {
    private var startTime:int;
    private var millisFromStart:int;
    private var millisTillEnd:int;
    private var points:Points;
    private var speed:Number;

    public function Tornado(id: int, startTime:int, millisFromStart:int, millisTillEnd:int, points:Points, speed:Number) {
        _id = id;
        this.startTime = startTime;
        this.millisFromStart = millisFromStart;
        this.millisTillEnd = millisTillEnd;
        this.points = points;
        this.speed = speed;
    }

    private var _id:int;

    public function get id():int {
        return _id;
    }

    public function pos(time:int):Point {
        const duration:int = time - startTime + millisFromStart;
        const distance:Number = duration * speed;
        return points.getPos(distance)
    }

    public function needRemove(time:int):Boolean {
        return time - startTime - millisFromStart > millisTillEnd;
    }
}
}

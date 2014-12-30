package ru.rknrl.castles.model.game {
public class Explosion {
    private static const duration:int = 500;
    private var _id:int;

    public function get id():int {
        return _id;
    }

    private var startTime:int;

    public function Explosion(id:int, startTime:int) {
        _id = id;
        this.startTime = startTime;
    }

    public function needRemove(time:int):Boolean {
        return time > startTime + duration;
    }
}
}

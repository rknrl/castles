package ru.rknrl.castles.model.game {
public class Volcano {
    private var startTime:int;
    private var millisTillEnd:int;

    public function Volcano(id:int, startTime:int, millisTillEnd:int) {
        _id = id;
        this.startTime = startTime;
        this.millisTillEnd = millisTillEnd;
    }

    private var _id:int;

    public function get id():int {
        return _id;
    }

    private static const radiuses:Vector.<Number> = new <Number>[20, 30, 40];

    public function radius(time:int):Number {
        const progress:Number = (time - startTime) / millisTillEnd;
        const index:int = Math.min(radiuses.length * progress, radiuses.length - 1);
        return radiuses[index];
    }

    public function needRemove(time:int):Boolean {
        return time - startTime > millisTillEnd;
    }
}
}

package ru.rknrl.castles.model.game {
public class Periodic {
    private var _startTime:int;

    protected function get startTime():int {
        return _startTime;
    }

    private var _millisTillEnd:int;

    protected function get millisTillEnd():int {
        return _millisTillEnd;
    }

    public function Periodic(startTime:int, millisTillEnd:int) {
        _startTime = startTime;
        _millisTillEnd = millisTillEnd;
    }

    public function needRemove(time:int):Boolean {
        return time > _startTime + _millisTillEnd;
    }
}
}

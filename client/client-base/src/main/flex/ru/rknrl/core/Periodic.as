//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.core {
public class Periodic {
    private var _startTime:int;

    public function get startTime():int {
        return _startTime;
    }

    private var _duration:int;

    public function get duration():int {
        return _duration;
    }

    public function Periodic(startTime:int, duration:int) {
        _startTime = startTime;
        _duration = duration;
    }

    public function needRemove(time:int):Boolean {
        return time - _startTime >= _duration;
    }

    public function progress(time:int):Number {
        return (time - _startTime) / _duration
    }
}
}

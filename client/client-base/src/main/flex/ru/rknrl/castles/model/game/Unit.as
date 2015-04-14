//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.game {
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.dto.PlayerId;
import ru.rknrl.dto.UnitId;

public class Unit {
    private var startPos:Point;
    private var endPos:Point;
    private var startTime:int;
    private var duration:int;

    public function Unit(id:UnitId, owner:PlayerId, startPos:Point, endPos:Point, startTime:int, duration:int, count:int) {
        _id = id;
        _owner = owner;
        this.startPos = startPos;
        this.endPos = endPos;
        this.startTime = startTime;
        this.duration = duration;
        setCount(count);
    }

    public function setCount(count:int):void {
        _count = count;
    }

    private var _id:UnitId;

    public function get id():UnitId {
        return _id;
    }

    private var _owner:PlayerId;

    public function get owner():PlayerId {
        return _owner;
    }

    private var _count:int;

    public function get count():int {
        return _count;
    }

    public function pos(time:int):Point {
        const progress:Number = Math.min(1, (time - startTime) / duration);
        return startPos.lerp(endPos, progress);
    }

    public function needRemove(time:int):Boolean {
        return time - startTime > duration;
    }
}
}

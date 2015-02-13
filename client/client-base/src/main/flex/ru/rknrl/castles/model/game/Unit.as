//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.game {
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.dto.UnitIdDTO;

public class Unit {
    private var startPos:Point;
    private var endPos:Point;
    private var startTime:int;
    private var speed:Number;

    public function Unit(id:UnitIdDTO, startPos:Point, endPos:Point, startTime:int, speed:Number) {
        _id = id;
        this.endPos = endPos;
        update(startTime, startPos, speed);
    }

    public function update(startTime:int, startPos:Point, speed:Number):void {
        this.startTime = startTime;
        this.startPos = startPos;
        this.speed = speed;
    }

    private var _id:UnitIdDTO;

    public function get id():UnitIdDTO {
        return _id;
    }

    private function get duration():int {
        return startPos.distance(endPos) / speed;
    }

    public function pos(time:int):Point {
        const progress:Number = (time - startTime) / duration;
        return startPos.lerp(endPos, progress);
    }
}
}

//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.game {
import ru.rknrl.castles.model.points.Point;

public class Movable extends Periodic {
    private var startPos:Point;
    private var endPos:Point;

    public function Movable(startPos:Point, endPos:Point, startTime:int, duration:int) {
        this.startPos = startPos;
        this.endPos = endPos;
        super(startTime, duration);
    }

    public function pos(time:int):Point {
        const progress:Number = (time - startTime) / millisTillEnd;
        return startPos.lerp(endPos, progress);
    }
}
}

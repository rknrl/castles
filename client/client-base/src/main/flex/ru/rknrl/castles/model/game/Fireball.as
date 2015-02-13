//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.game {
import ru.rknrl.castles.model.points.Point;

public class Fireball extends Movable {
    private var _id:int;

    public function get id():int {
        return _id;
    }

    public function Fireball(id:int, startPos:Point, endPos:Point, startTime:int, duration:int) {
        _id = id;
        super(startPos, endPos, startTime, duration);
    }
}
}

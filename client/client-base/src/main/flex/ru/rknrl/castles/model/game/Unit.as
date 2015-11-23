//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.game {
import ru.rknrl.core.Movable;
import ru.rknrl.core.points.Points;
import protos.PlayerId;

public class Unit extends Movable {
    public var count:int;

    public function Unit(owner:PlayerId, points:Points, startTime:int, duration:int, count:int) {
        _owner = owner;
        this.count = count;
        super(points, startTime, duration);
    }

    private var _owner:PlayerId;

    public function get owner():PlayerId {
        return _owner;
    }
}
}

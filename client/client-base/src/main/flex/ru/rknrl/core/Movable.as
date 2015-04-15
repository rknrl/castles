//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.core {
import ru.rknrl.core.points.Point;
import ru.rknrl.core.points.Points;

public class Movable extends Periodic implements GameObject {
    private var points:Points;

    public function Movable(points:Points, startTime:int, duration:int) {
        this.points = points;
        super(startTime, duration);
    }

    public function pos(time:int):Point {
        return points.pos(progress(time));
    }
}
}

//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.core {
import ru.rknrl.core.points.Point;

public class Static extends Periodic implements GameObject {
    private var point:Point;

    public function Static(point:Point, startTime:int, duration:int) {
        this.point = point;
        super(startTime, duration);
    }

    public function pos(time:int):Point {
        return point;
    }
}
}

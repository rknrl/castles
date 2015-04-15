//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.core {
import org.flexunit.asserts.assertTrue;

import ru.rknrl.core.points.Point;
import ru.rknrl.core.points.Points;

public class MovableTest {
    [Test("pos")]
    public function t1():void {
        const points:Points = Points.two(new Point(1, 2), new Point(3, 4));
        const movable:Movable = new Movable(points, 1, 10);
        assertTrue(movable.pos(5).equals(points.pos(movable.progress(5))))
    }
}
}

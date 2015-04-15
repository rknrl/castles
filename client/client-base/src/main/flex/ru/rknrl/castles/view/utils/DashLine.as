//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.utils {
import flash.display.Graphics;

import ru.rknrl.core.points.Point;
import ru.rknrl.core.points.Points;

public class DashLine {
    public static function drawPath(g:Graphics, points:Points, distance:Number, dashLength:int = 10, gap:int = 10):void {
        if (distance == Number.POSITIVE_INFINITY) throw new Error("distance is positive infinity");

        var d:Number = 0;
        while (d < distance) {
            const p1:Point = points.pos(d);
            g.moveTo(p1.x, p1.y);

            const p2:Point = points.pos(d + dashLength);
            g.lineTo(p2.x, p2.y);

            d += dashLength + gap;
        }
    }
}
}

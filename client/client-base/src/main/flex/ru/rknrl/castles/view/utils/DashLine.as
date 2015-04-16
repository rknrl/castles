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
    public static function drawPath(g:Graphics, points:Points, progress:Number, dashLengthPx:int = 10, gapPx:int = 10):void {
        if (progress == Number.POSITIVE_INFINITY) throw new Error("progress is positive infinity");

        if (points.totalDistance == 0) return;
        const dashLength:Number = dashLengthPx / points.totalDistance;
        const gap:Number = gapPx / points.totalDistance;

        var d:Number = 0;
        while (d < progress) {
            const p1:Point = points.pos(d);
            g.moveTo(p1.x, p1.y);

            const p2:Point = points.pos(d + dashLength);
            g.lineTo(p2.x, p2.y);

            d += dashLength + gap;
        }
    }
}
}

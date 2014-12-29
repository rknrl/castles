package ru.rknrl.castles.view.utils {
import flash.display.Graphics;

import ru.rknrl.castles.utils.points.Point;
import ru.rknrl.castles.utils.points.Points;

public class DashLine {
    public static function drawPath(g:Graphics, points:Points, dashLength:int = 10, gap:int = 10):void {
        const distance:Number = points.totalDistance;

        var d:Number = 0;
        while (d < distance) {
            const p1:Point = points.getPos(d);
            g.moveTo(p1.x, p1.y);

            const p2:Point = points.getPos(d + dashLength);
            g.lineTo(p2.x, p2.y);

            d += dashLength + gap;
        }
    }
}
}

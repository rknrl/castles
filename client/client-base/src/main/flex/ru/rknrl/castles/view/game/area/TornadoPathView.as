package ru.rknrl.castles.view.game.area {
import flash.display.CapsStyle;
import flash.display.LineScaleMode;
import flash.display.Sprite;

import ru.rknrl.castles.utils.points.Point;
import ru.rknrl.castles.utils.points.Points;
import ru.rknrl.castles.view.utils.DashLine;

public class TornadoPathView extends Sprite {
    public function drawPath(tornadoPoints:Vector.<Point>):void {
        graphics.clear();
        graphics.lineStyle(5, 0xffffff, 1, false, LineScaleMode.NORMAL, CapsStyle.NONE);

        if (tornadoPoints.length >= 2) {
            const points:Points = new Points(tornadoPoints);
            DashLine.drawPath(graphics, points);
        }
    }

    public function clear():void {
        graphics.clear();
    }
}
}

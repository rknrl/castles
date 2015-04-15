//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game.area {
import flash.display.CapsStyle;
import flash.display.LineScaleMode;
import flash.display.Sprite;

import ru.rknrl.core.points.Points;
import ru.rknrl.castles.view.utils.DashLine;

public class TornadoPathView extends Sprite {
    public function drawPath(points:Points, distance:Number):void {
        graphics.clear();
        graphics.lineStyle(5, 0xffffff, 1, false, LineScaleMode.NORMAL, CapsStyle.NONE);

        DashLine.drawPath(graphics, points, distance);
    }

    public function clear():void {
        graphics.clear();
    }
}
}

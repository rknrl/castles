//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game.area.arrows {
import flash.display.DisplayObject;
import flash.display.Sprite;

import ru.rknrl.core.points.Point;

public class ArrowView extends Sprite {
    private var head:DisplayObject;
    private var body:DisplayObject;
    private var bodyOriginalHeight:Number;

    public function ArrowView(startPos:Point) {
        x = startPos.x;
        y = startPos.y;

        addChild(body = new ArrowBodyMC());
        addChild(head = new ArrowHeadMC());

        bodyOriginalHeight = body.height;
    }

    public function orient(endPos:Point):void {
        const dx:Number = endPos.x - x;
        const dy:Number = endPos.y - y;
        const distance:Number = Math.sqrt(dx * dx + dy * dy);

        body.visible = distance > head.height;
        body.scaleY = (distance - head.height) / bodyOriginalHeight;

        head.y = -distance + head.height + 1;

        const angle:Number = Math.atan2(dx, -dy);
        rotation = angle * 180 / Math.PI;
    }
}
}

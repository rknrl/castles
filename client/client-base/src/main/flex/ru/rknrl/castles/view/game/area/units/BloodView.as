//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game.area.units {
import flash.display.Shape;
import flash.display.Sprite;

import ru.rknrl.castles.model.points.Point;

public class BloodView extends Sprite {
    public function addBlood(pos:Point):void {
        const shape:Shape = new Shape();
        shape.graphics.beginFill(0xff0000);
        shape.graphics.drawEllipse(-2, -1, 4, 2);
        shape.graphics.endFill();
        shape.x = pos.x;
        shape.y = pos.y;
        addChild(shape);
    }
}
}

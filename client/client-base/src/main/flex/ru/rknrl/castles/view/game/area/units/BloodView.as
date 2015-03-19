//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game.area.units {
import flash.display.Sprite;

import ru.rknrl.castles.model.points.Point;

public class BloodView extends Sprite {
    public function addBlood(pos:Point, color:uint):void {
        const unitKill:UnitKill = new UnitKill(color);
        unitKill.x = pos.x;
        unitKill.y = pos.y;
        addChild(unitKill);
    }
}
}

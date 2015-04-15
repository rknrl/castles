//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.controller.game {
import flash.display.Sprite;

import ru.rknrl.castles.view.game.area.VolcanoView;
import ru.rknrl.core.GameObject;
import ru.rknrl.core.GameObjectsController;

public class VolcanoesController extends GameObjectsController {
    public function VolcanoesController(layer:Sprite) {
        super(layer);
    }

    override protected function updateObject(time:int, object:GameObject):void {
        super.updateObject(time, object);
        const view:VolcanoView = objectToView[object];
        view.radius = getRadius(time, object);
    }

    private static const radiuses:Vector.<Number> = new <Number>[20, 30, 40];

    private static function getRadius(time:int, object:GameObject):Number {
        const progress:Number = (time - object.startTime) / object.duration;
        const index:int = Math.min(radiuses.length * progress, radiuses.length - 1);
        return radiuses[index];
    }
}
}

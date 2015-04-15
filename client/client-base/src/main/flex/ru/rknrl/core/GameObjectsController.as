//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.core {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.utils.Dictionary;

import ru.rknrl.core.points.Point;

public class GameObjectsController {
    private var layer:Sprite;
    public const objectToView:Dictionary = new Dictionary();

    public function GameObjectsController(layer:Sprite) {
        this.layer = layer;
    }

    public function add(time:int, object:GameObject, view:DisplayObject):void {
        if (objectToView[object]) throw new Error("add but already exists " + object);
        objectToView[object] = view;
        layer.addChild(view);
        updateObject(time, object);
    }

    public function remove(time:int, object:GameObject):void {
        const view:DisplayObject = objectToView[object];
        if (view) {
            layer.removeChild(view);
            delete objectToView[object];
        }
    }

    protected function updateObject(time:int, object:GameObject):void {
        const pos:Point = object.pos(time);
        const view:DisplayObject = objectToView[object];
        view.x = pos.x;
        view.y = pos.y;
    }

    public function update(time:int):void {
        const toRemove:Vector.<GameObject> = new <GameObject>[];
        for (var object:GameObject in objectToView) {
            if (object.isFinish(time)) {
                toRemove.push(object);
            } else {
                updateObject(time, object);
            }
        }

        for each(object in toRemove) remove(time, object)
    }
}
}

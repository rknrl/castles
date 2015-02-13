//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game.area {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.utils.Dictionary;

import ru.rknrl.castles.model.points.Point;

public class PeriodicView extends Sprite {
    private var _name:String;

    public function PeriodicView(name:String) {
        _name = name;
    }

    private const map:Dictionary = new Dictionary();

    protected final function byId(id:int):DisplayObject {
        const displayObject:DisplayObject = map[id];
        if (!displayObject) throw new Error("can't find " + _name + " " + id);
        return displayObject;
    }

    protected final function add(id:int, pos:Point, displayObject:DisplayObject):void {
        if (map[id]) throw new Error(_name + " " + id + " already exists");
        map[id] = displayObject;
        displayObject.x = pos.x;
        displayObject.y = pos.y;
        addChild(displayObject);
    }

    public function remove(id:int):void {
        removeChild(byId(id));
        delete map[id];
    }
}
}

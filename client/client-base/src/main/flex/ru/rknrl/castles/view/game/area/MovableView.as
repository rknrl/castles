package ru.rknrl.castles.view.game.area {
import flash.display.DisplayObject;

import ru.rknrl.castles.model.points.Point;

public class MovableView extends PeriodicView {
    public function MovableView(name:String) {
        super(name);
    }

    public function setPos(id:int, pos:Point):void {
        const displayObject:DisplayObject = byId(id);
        displayObject.x = pos.x;
        displayObject.y = pos.y;
    }
}
}

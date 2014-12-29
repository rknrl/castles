package ru.rknrl.castles.view.game.area.bullets {
import flash.display.Shape;
import flash.display.Sprite;

import ru.rknrl.castles.utils.points.Point;

public class BulletView extends Sprite {
    private var shape:Shape;

    public function BulletView(pos:Point) {
        shape = new Shape();
        shape.graphics.beginFill(0);
        shape.graphics.drawCircle(0, 0, 5);
        shape.graphics.endFill();
        addChild(shape);
        this.pos = pos;
    }

    public function set pos(value:Point):void {
        shape.x = value.x;
        shape.y = value.y;
    }
}
}

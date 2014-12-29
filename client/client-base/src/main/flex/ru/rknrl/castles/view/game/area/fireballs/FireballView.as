package ru.rknrl.castles.view.game.area.fireballs {
import flash.display.Shape;
import flash.display.Sprite;

import ru.rknrl.castles.utils.points.Point;
import ru.rknrl.utils.createCircle;

public class FireballView extends Sprite {
    private static const radius:Number = 10;
    private var ball:Shape;

    public function FireballView(pos:Point) {
        alpha = 0.7;
        addChild(ball = createCircle(radius, 0x000000));
        this.pos = pos;
    }

    public function set pos(value:Point):void {
        ball.x = value.x;
        ball.y = value.y;
    }
}
}

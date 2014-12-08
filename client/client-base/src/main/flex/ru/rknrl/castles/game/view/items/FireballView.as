package ru.rknrl.castles.game.view.items {
import flash.display.Shape;
import flash.display.Sprite;

import ru.rknrl.easers.IEaser;
import ru.rknrl.easers.Linear;
import ru.rknrl.utils.createCircle;

public class FireballView extends Sprite {
    private static const radius:Number = 10;
    private static const explosionRadius:Number = 32;

    private var flyDuration:int;
    private static const explosionDuration:int = 300;

    public function get duration():int {
        return flyDuration + explosionDuration;
    }

    private var _startTime:int;

    public function get startTime():int {
        return _startTime;
    }

    private var ball:Shape;
    private var explosions:Vector.<Shape> = new <Shape>[];
    private var dx:Number;
    private var dy:Number;

    public function FireballView(startTime:int, flyDuration:int, dx:Number, dy:Number) {
        this._startTime = startTime;
        this.dx = dx;
        this.dy = dy;

        alpha = 0.7;

        this.flyDuration = flyDuration;

        addChild(ball = createCircle(radius, 0x000000));
        ball.x = dx;
        ball.y = dy;

        const offset:int = explosionRadius / 2;
        addChild(explosions[0] = addExplosion(-offset, -offset, radius));
        addChild(explosions[1] = addExplosion(0, offset, radius));
        addChild(explosions[2] = addExplosion(offset, -offset, radius));
    }

    private static function addExplosion(x:Number, y:Number, radius:int):Shape {
        const explosion:Shape = createCircle(radius, 0xff0000);
        explosion.x = x;
        explosion.y = y;
        explosion.alpha = 0.7;
        explosion.scaleX = explosion.scaleY = 0;
        return explosion;
    }

    public static const easer:IEaser = new Linear(0, 1);

    public function update(time:int):void {
        const p:Number = (1 - (time - startTime) / flyDuration);
        ball.x = dx * p;
        ball.y = dy * p;

        if (time - startTime > flyDuration) {
            ball.visible = false;
            for (var i:int = 0; i < explosions.length; i++) {
                const explosion:Shape = explosions[i];
                var fraction:Number = (time - startTime - flyDuration) / (explosionDuration / (i + 1));
                if (fraction > 1) explosion.visible = false;
                explosion.scaleX = explosion.scaleY = easer.ease(fraction);
            }
        }
    }
}
}

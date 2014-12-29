package ru.rknrl.castles.view.game.area.fireballs.explosions {
import flash.display.Shape;
import flash.display.Sprite;
import flash.events.Event;
import flash.utils.getTimer;

import ru.rknrl.easers.IEaser;
import ru.rknrl.easers.Linear;
import ru.rknrl.utils.createCircle;

public class ExplosionView extends Sprite {
    public static const radius:Number = 32;
    public static const duration:int = 300;

    private const shapes:Vector.<Shape> = new <Shape>[];
    private var startTime:int;

    public function ExplosionView() {
        alpha = 0.7;

        const offset:int = radius / 2;
        addChild(shapes[0] = createExplosion(-offset, -offset, radius));
        addChild(shapes[1] = createExplosion(0, offset, radius));
        addChild(shapes[2] = createExplosion(offset, -offset, radius));

        startTime = getTimer();
        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    private static function createExplosion(x:Number, y:Number, radius:int):Shape {
        const explosion:Shape = createCircle(radius, 0xff0000);
        explosion.x = x;
        explosion.y = y;
        explosion.alpha = 0.7;
        explosion.scaleX = explosion.scaleY = 0;
        return explosion;
    }

    public static const easer:IEaser = new Linear(0, 1);

    private function onEnterFrame(event:Event):void {
        for (var i:int = 0; i < shapes.length; i++) {
            const explosion:Shape = shapes[i];
            var fraction:Number = (getTimer() - startTime) / (duration / (i + 1));
            if (fraction > 1) explosion.visible = false;
            explosion.scaleX = explosion.scaleY = easer.ease(fraction);
        }
    }
}
}

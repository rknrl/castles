//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.utils.dust {
import flash.display.Shape;

public class FireDustShape extends Shape {
    private static const maxDuration:int = 5000;
    private static const minDuration:int = 2500;

    private static const minRadius:Number = 5;
    private static const maxRadius:Number = 15;

    private static const minDx:Number = -16.0 / 1000;
    private static const maxDx:Number = 16.0 / 1000;

    private static const minDy:Number = -32.0 / 1000;
    private static const maxDy:Number = 0;

    private var dScale:Number;
    private var dy:Number;
    private var dx:Number;

    public function FireDustShape() {
        const radius:Number = minRadius + Math.random() * (maxRadius - minRadius);
        graphics.beginFill(0xcccccc);
        graphics.drawCircle(0, 0, radius);
        graphics.endFill();
        init();
    }

    public function init():void {
        scaleX = scaleY = alpha = 1;

        const duration:int = minDuration + Math.random() * (maxDuration - minDuration);
        dScale = -1 / duration;
        dx = minDx + Math.random() * (maxDx - minDx);
        dy = minDy + Math.random() * (maxDy - minDy);
        _needRemove = false;
    }

    public function enterFrame(deltaTime:int):void {
        if (scaleX > 0) {
            scaleX += dScale * deltaTime;
            scaleY += dScale * deltaTime;
            alpha += dScale * deltaTime;
        } else {
            _needRemove = true;
        }
        x += dx * deltaTime;
        y += dy * deltaTime;
    }

    private var _needRemove:Boolean;

    public function get needRemove():Boolean {
        return _needRemove;
    }
}
}

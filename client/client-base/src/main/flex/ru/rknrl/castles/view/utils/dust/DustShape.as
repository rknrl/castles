//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.utils.dust {
import flash.display.Shape;

public class DustShape extends Shape {
    private static const duration:int = 5000;
    public static const dScale:Number = -1 / duration;

    private static const minRadius:Number = 20;
    private static const maxRadius:Number = 60;

    private static const minDx:Number = -16.0 / 1000;
    private static const maxDx:Number = 16.0 / 1000;

    private var dy:Number;
    private var dx:Number;

    public function DustShape() {
        const radius:Number = minRadius + Math.random() * (maxRadius - minRadius);
        graphics.beginFill(0xcccccc);
        graphics.drawCircle(0, 0, radius);
        graphics.endFill();

        dx = minDx + Math.random() * (maxDx - minDx);
        dy = minDx + Math.random() * (maxDx - minDx);
    }

    public function enterFrame(deltaTime:int):void {
        if (scaleX > 0) {
            scaleX += dScale * deltaTime;
            scaleY += dScale * deltaTime;
        }
        x += dx * deltaTime;
        y += dy * deltaTime;
    }
}
}

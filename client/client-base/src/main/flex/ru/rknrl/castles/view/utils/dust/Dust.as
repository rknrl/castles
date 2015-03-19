//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.utils.dust {
import flash.display.Sprite;

public class Dust extends Sprite {
    private var shapes:Vector.<DustShape> = new <DustShape>[];

    private static const minDistance:Number = -60;
    private static const maxDistance:Number = 60;

    public function Dust() {
        for (var i:int = 0; i < 30; i++) {
            const shape:DustShape = new DustShape();
            shape.x = minDistance + Math.random() * (maxDistance - minDistance);
            shape.y = minDistance + Math.random() * (maxDistance - minDistance);
            addChild(shape);
            shapes.push(shape);
        }
    }

    public function enterFrame(deltaTime:int):void {
        for each(var shape:DustShape in shapes) shape.enterFrame(deltaTime);
    }
}
}

//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.utils.dust {
import flash.display.Sprite;

public class FireDust extends Sprite {
    private var shapes:Vector.<FireDustShape> = new <FireDustShape>[];

    public function FireDust() {
        for (var i:int = 0; i < 10; i++) {
            const shape:FireDustShape = new FireDustShape();
            shape.x = 0;
            shape.y = 0;
            addChild(shape);
            shapes.push(shape);
        }
    }

    public function enterFrame(deltaTime:int):void {
        for each(var shape:FireDustShape in shapes) {
            shape.enterFrame(deltaTime);
            if (shape.needRemove) {
                shape.x = 0;
                shape.y = 0;
                shape.init();
            }
        }
    }
}

}

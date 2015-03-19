//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.utils.dust {
import flash.display.Sprite;
import flash.events.Event;
import flash.utils.getTimer;

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
        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    private var lastTime:int;

    private function onEnterFrame(event:Event):void {
        const time:int = getTimer();
        const deltaTime:int = time - lastTime;
        lastTime = time;

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

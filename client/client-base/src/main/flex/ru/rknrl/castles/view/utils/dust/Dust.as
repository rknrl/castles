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

public class Dust extends Sprite {
    private var shapes:Vector.<DustShape> = new <DustShape>[];

    private static const minDistance:Number = -60;
    private static const maxDistance:Number = 60;

    public function Dust(startTime:int) {
        lastTime = startTime;
        for (var i:int = 0; i < 30; i++) {
            const shape:DustShape = new DustShape();
            shape.x = minDistance + Math.random() * (maxDistance - minDistance);
            shape.y = minDistance + Math.random() * (maxDistance - minDistance);
            addChild(shape);
            shapes.push(shape);
        }

        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    private var lastTime:int;

    private function onEnterFrame(e:Event):void {
        const time:int = getTimer();
        const deltaTime:int = time - lastTime;
        lastTime = time;

        for each(var shape:DustShape in shapes) shape.enterFrame(deltaTime);
    }
}
}

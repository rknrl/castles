//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package {
import flash.display.Sprite;
import flash.events.Event;
import flash.events.MouseEvent;
import flash.utils.getTimer;

public class DustDemo extends Sprite {
    private var dusts:Vector.<Dust> = new <Dust>[];

    public function DustDemo() {
        stage.addEventListener(MouseEvent.CLICK, onClick);
        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    private var lastTime:int;

    private function onEnterFrame(event:Event):void {
        const time:int = getTimer();
        const deltaTime:int = time - lastTime;
        lastTime = time;
        for each(var dust:Dust in dusts) dust.enterFrame(deltaTime);
    }

    private function onClick(event:MouseEvent):void {
        const dust:Dust = new Dust();
        dust.x = mouseX;
        dust.y = mouseY;
        addChild(dust);
        dusts.push(dust);
    }
}
}

import flash.display.Shape;
import flash.display.Sprite;

class Dust extends Sprite {
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

class DustShape extends Shape {
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
package ru.rknrl.castles.game.view.items {
import flash.display.Sprite;

import ru.rknrl.utils.drawCircle;

public class VolcanoView extends Sprite {
    private static const radiuses:Vector.<Number> = new <Number>[10, 20, 40];

    private var _startTime:int;

    public function get startTime():int {
        return _startTime;
    }

    private var _millisTillEnd:int;

    public function get millisTillEnd():int {
        return _millisTillEnd;
    }

    private var radiusIndex:int;

    public function VolcanoView(startTime:int, millisTillEnd:int) {
        _startTime = startTime;
        _millisTillEnd = millisTillEnd;

        radiusIndex = 0;
        alpha = 0.7;
        draw()
    }

    private function getCurrentRadiusIndex(time:int):int {
        const progress:Number = (time - startTime) / millisTillEnd;
        const index:int = Math.min(radiuses.length * progress, radiuses.length - 1);
        return index;
    }

    private function draw():void {
        drawCircle(graphics, radiuses[radiusIndex], 0xff0000);
    }

    public function update(time:int):void {
        const index:int = getCurrentRadiusIndex(time);
        if (radiusIndex != index) {
            radiusIndex = index;
            graphics.clear();
            draw();
        }

        scaleX = scaleY = 1 + Math.sin(time / 100) * 0.1;
    }
}
}

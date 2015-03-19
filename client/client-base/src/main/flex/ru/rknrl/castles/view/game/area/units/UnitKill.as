//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game.area.units {
import flash.display.MovieClip;
import flash.display.Sprite;
import flash.events.Event;
import flash.utils.getTimer;

import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.Fla;
import ru.rknrl.castles.view.utils.Shadow;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;

public class UnitKill extends Sprite {
    private static const duration:int = 500;

    private var unit:MovieClip;
    private var shadow:Shadow;
    private var startTime:int;
    private var dx:Number;
    private var color:uint;

    public function UnitKill(color:uint) {
        this.color = color;

        addChild(shadow = new Shadow());
        shadow.width = 16;
        shadow.height = 2;

        unit = Fla.unit(BuildingType.TOWER, BuildingLevel.LEVEL_1);
        unit.transform.colorTransform = Colors.transform(color);
        addChild(unit);
        unit.stop();

        dx = Math.random() > 0.5 ? -0.1 : 0.1;

        startTime = getTimer();
        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    private function onEnterFrame(event:Event):void {
        const time:int = getTimer();
        const deltaTime:int = time - startTime;


        if (deltaTime < duration) {
            update(deltaTime)
        } else {
            update(duration);
            unit.transform.colorTransform = Colors.transform(Colors.light(color));
            removeEventListener(Event.ENTER_FRAME, onEnterFrame);
        }
    }

    private function update(deltaTime:int):void {
        unit.rotation = deltaTime / duration * 270;
        unit.x = deltaTime * dx;
        unit.y = -Math.sin(deltaTime / duration * (Math.PI - 0.05)) * 48;

        shadow.x = deltaTime * (dx > 0 ? dx * 0.92 : dx * 1.08);
    }
}
}

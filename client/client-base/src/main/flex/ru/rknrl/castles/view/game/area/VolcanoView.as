//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game.area {
import flash.display.Sprite;
import flash.events.Event;

import ru.rknrl.castles.model.events.GameViewEvents;

public class VolcanoView extends Sprite {
    private var originalSize:Number;

    public function VolcanoView() {
        var volcanoMC:VolcanoMC = new VolcanoMC();
        originalSize = volcanoMC.width;
        addChild(volcanoMC);
    }

    private var _radius:Number;

    public function get radius():Number {
        return _radius;
    }

    public function set radius(value:Number):void {
        if (_radius != value) dispatchEvent(new Event(GameViewEvents.SHAKE, true));
        _radius = value;
        scaleX = scaleY = value / originalSize;
    }
}
}

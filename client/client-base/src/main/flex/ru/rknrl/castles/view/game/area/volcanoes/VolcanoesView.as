package ru.rknrl.castles.view.game.area.volcanoes {
import flash.display.Sprite;
import flash.utils.Dictionary;

import ru.rknrl.castles.utils.points.Point;

public class VolcanoesView extends Sprite {
    private const volcanoes:Dictionary = new Dictionary();

    public function addVolcano(id:int, pos:Point, radius:Number):void {
        const volcano:VolcanoView = new VolcanoView(pos, radius);
        addChild(volcano);
        volcanoes[id] = volcano;
    }

    public function setVolcanoRadius(id:int, radius:Number):void {
        const volcano:VolcanoView = volcanoes[id];
        volcano.radius = radius;
    }

    public function removeVolcano(id:int):void {
        const volcano:VolcanoView = volcanoes[id];
        removeChild(volcano);
        delete volcanoes[id];
    }
}
}

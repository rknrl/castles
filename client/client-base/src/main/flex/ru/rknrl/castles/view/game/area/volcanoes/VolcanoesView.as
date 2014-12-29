package ru.rknrl.castles.view.game.area.volcanoes {
import flash.display.Sprite;
import flash.utils.Dictionary;

import ru.rknrl.castles.utils.points.Point;

public class VolcanoesView extends Sprite {
    private const volcanoes:Dictionary = new Dictionary();

    private function byId(id:int):VolcanoView {
        const volcano:VolcanoView = volcanoes[id];
        if (!volcano) throw new Error("can't find volcano " + id);
        return volcano;
    }

    public function addVolcano(id:int, pos:Point, radius:Number):void {
        const volcano:VolcanoView = new VolcanoView(pos, radius);
        if (volcanoes[id]) throw new Error("volcano " + id + " already exists");
        volcanoes[id] = volcano;
        addChild(volcano);
    }

    public function setVolcanoRadius(id:int, radius:Number):void {
        byId(id).radius = radius;
    }

    public function removeVolcano(id:int):void {
        removeChild(byId(id));
        delete volcanoes[id];
    }
}
}

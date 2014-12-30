package ru.rknrl.castles.view.game.area.tornadoes {
import flash.display.Sprite;
import flash.utils.Dictionary;

import ru.rknrl.castles.utils.points.Point;

public class TornadoesView extends Sprite {
    private const tornadoes:Dictionary = new Dictionary();

    private function byId(id:int):TornadoMC {
        const tornado:TornadoMC = tornadoes[id];
        if (!tornado) throw new Error("can't find tornado " + id);
        return tornado;
    }

    public function addTornado(id:int, pos:Point):void {
        const tornado:TornadoMC = new TornadoMC();
        if (tornadoes[id]) throw new Error("tornado " + id + " already exists");
        tornadoes[id] = tornado;
        addChild(tornado);
        setTornadoPos(id, pos);
    }

    public function setTornadoPos(id:int, pos:Point):void {
        byId(id).x = pos.x;
        byId(id).y = pos.y;
    }

    public function removeTornado(id:int):void {
        removeChild(byId(id));
        delete tornadoes[id];
    }
}
}

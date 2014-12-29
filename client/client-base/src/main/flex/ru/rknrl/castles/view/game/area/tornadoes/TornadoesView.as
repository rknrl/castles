package ru.rknrl.castles.view.game.area.tornadoes {
import flash.display.Sprite;
import flash.utils.Dictionary;

import ru.rknrl.castles.utils.points.Point;

public class TornadoesView extends Sprite {
    private const tornadoes:Dictionary = new Dictionary();

    public function addTornado(id:int, pos:Point):void {
        const tornado:TornadoView = new TornadoView(pos);
        addChild(tornado);
        tornadoes[id] = tornado;
    }

    public function setTornadoPos(id:int, pos:Point):void {
        const tornado:TornadoView = tornadoes[id];
        tornado.pos = pos;
    }

    public function removeTornado(id:int):void {
        const tornado:TornadoView = tornadoes[id];
        removeChild(tornado);
        delete tornadoes[id];
    }
}
}

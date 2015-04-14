//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.controller.game {
import flash.utils.getTimer;

import ru.rknrl.castles.model.game.Tornado;
import ru.rknrl.castles.model.points.Points;
import ru.rknrl.castles.view.game.area.TornadoesView;
import ru.rknrl.dto.TornadoDTO;

public class Tornadoes {
    private var view:TornadoesView;

    public function Tornadoes(view:TornadoesView) {
        this.view = view;
    }

    public const tornadoes:Vector.<Tornado> = new <Tornado>[];
    private var tornadoIterator:int;

    public function add(dto:TornadoDTO):void {
        const time:int = getTimer();
        const tornado:Tornado = new Tornado(tornadoIterator++, time, dto.millisFromStart, dto.millisTillEnd, Points.fromDto(dto.points));
        tornadoes.push(tornado);
        view.addTornado(tornado.id, tornado.pos(time))
    }

    public function update(time:int):void {
        const toRemove:Vector.<Tornado> = new <Tornado>[];

        for each(var tornado:Tornado in tornadoes) {
            view.setPos(tornado.id, tornado.pos(time));
            if (tornado.needRemove(time)) toRemove.push(tornado);
        }

        for each(tornado in toRemove) {
            const index:int = tornadoes.indexOf(tornado);
            tornadoes.splice(index, 1);
            view.remove(tornado.id);
        }
    }

}
}

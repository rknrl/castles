//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.controller.game {
import flash.utils.getTimer;

import ru.rknrl.castles.model.game.Volcano;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.view.game.area.volcanoes.VolcanoesView;
import ru.rknrl.dto.VolcanoDTO;

public class Volcanoes {
    private var view:VolcanoesView;

    public function Volcanoes(view:VolcanoesView) {
        this.view = view;
    }

    public const volcanoes:Vector.<Volcano> = new <Volcano>[];
    private var volcanoIterator:int;

    public function add(dto:VolcanoDTO):void {
        const time:int = getTimer();
        const pos:Point = new Point(dto.pos.x, dto.pos.y);
        const volcano:Volcano = new Volcano(volcanoIterator++, pos, time, dto.millisTillEnd);
        volcanoes.push(volcano);
        view.addVolcano(volcano.id, pos, volcano.radius(time));
    }

    public function update(time:int):void {
        const toRemove:Vector.<Volcano> = new <Volcano>[];

        for each(var volcano:Volcano in volcanoes) {
            view.setVolcanoRadius(volcano.id, volcano.radius(time));
            if (volcano.needRemove(time)) toRemove.push(volcano);
        }

        for each(volcano in toRemove) {
            const index:int = volcanoes.indexOf(volcano);
            volcanoes.splice(index, 1);
            view.remove(volcano.id);
        }
    }
}
}

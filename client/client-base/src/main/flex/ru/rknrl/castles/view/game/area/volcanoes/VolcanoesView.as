//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game.area.volcanoes {
import flash.events.Event;

import ru.rknrl.castles.model.events.GameViewEvents;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.view.game.area.PeriodicView;

public class VolcanoesView extends PeriodicView {
    public function VolcanoesView() {
        super("volcano");
    }

    public function addVolcano(id:int, pos:Point, radius:Number):void {
        add(id, pos, new VolcanoView(radius));
        dispatchEvent(new Event(GameViewEvents.SHAKE, true));
    }

    public function setVolcanoRadius(id:int, radius:Number):void {
        const volcano:VolcanoView = VolcanoView(byId(id));
        if (volcano.radius != radius) {
            volcano.radius = radius;
            dispatchEvent(new Event(GameViewEvents.SHAKE, true));
        }
    }
}
}

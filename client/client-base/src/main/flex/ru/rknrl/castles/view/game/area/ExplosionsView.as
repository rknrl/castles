//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game.area {
import flash.events.Event;

import ru.rknrl.castles.model.events.GameViewEvents;

import ru.rknrl.castles.model.points.Point;

public class ExplosionsView extends PeriodicView {
    public function ExplosionsView() {
        super("explosion");
    }

    public function addExplosion(id:int, pos:Point):void {
        add(id, pos, new ExplosionMC());
        dispatchEvent(new Event(GameViewEvents.SHAKE, true));
    }
}
}

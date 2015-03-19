//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game.area {
import flash.events.Event;
import flash.utils.getTimer;

import ru.rknrl.castles.model.events.GameViewEvents;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.view.utils.dust.Dust;
import ru.rknrl.castles.view.utils.dust.DustShape;

public class ExplosionsView extends PeriodicView {
    private var dusts:Vector.<Dust> = new <Dust>[];

    public function ExplosionsView() {
        super("explosion");
        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    public function addExplosion(id:int, pos:Point):void {
        const dust:Dust = new Dust(getTimer());
        dust.x = pos.x;
        dust.y = pos.y;
        addChild(dust);
        dusts.push(dust);

        add(id, pos, new ExplosionMC());

        dispatchEvent(new Event(GameViewEvents.SHAKE, true));
    }

    private var lastTime:int;

    private function onEnterFrame(event:Event):void {
        const time:int = getTimer();
        const deltaTime:int = time - lastTime;
        lastTime = time;

        const toRemove:Vector.<Dust> = new <Dust>[];

        for each(var dust:Dust in dusts) {
            dust.enterFrame(deltaTime);
            if (time - dust.startTime > DustShape.duration) toRemove.push(dust);
        }

        for each(dust in toRemove) {
            removeChild(dust);
            dusts.splice(dusts.indexOf(dust), 1);
        }
    }
}
}

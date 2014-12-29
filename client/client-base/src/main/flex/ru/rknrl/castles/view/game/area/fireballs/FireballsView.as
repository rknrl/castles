package ru.rknrl.castles.view.game.area.fireballs {
import flash.display.Sprite;
import flash.utils.Dictionary;

import ru.rknrl.castles.utils.points.Point;
import ru.rknrl.castles.view.game.area.fireballs.explosions.ExplosionsView;

public class FireballsView extends Sprite {
    public var explosions:ExplosionsView;

    public function FireballsView() {
        addChild(explosions = new ExplosionsView());
    }

    private const fireballs:Dictionary = new Dictionary();

    private function byId(id:int):FireballView {
        const fireball:FireballView = fireballs[id];
        if (!fireball) throw new Error("can't find fireball " + id);
        return fireball;
    }

    public function addFireball(id:int, pos:Point):void {
        const fireball:FireballView = new FireballView(pos);
        if (fireballs[id]) throw new Error("fireball " + id + " already exists");
        fireballs[id] = fireball;
        addChild(fireball);
    }

    public function setFireballPos(id:int, pos:Point):void {
        byId(id).pos = pos;
    }

    public function removeFireball(id:int):void {
        removeChild(byId(id));
        delete fireballs[id];
    }
}
}

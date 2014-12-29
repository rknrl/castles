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

    public function addFireball(id:int, pos:Point):void {
        const fireball:FireballView = new FireballView(pos);
        addChild(fireball);
        fireballs[id] = fireball;
    }

    public function setFireballPos(id:int, pos:Point):void {
        const fireball:FireballView = fireballs[id];
        fireball.pos = pos;
    }

    public function removeFireball(id:int):void {
        const fireball:FireballView = fireballs[id];
        removeChild(fireball);
        delete fireballs[id];
    }
}
}

package ru.rknrl.castles.view.game.area.fireballs.explosions {
import flash.display.Sprite;
import flash.utils.Dictionary;

import ru.rknrl.castles.utils.points.Point;

public class ExplosionsView extends Sprite {
    private const explosions:Dictionary = new Dictionary();

    private function byId(id:int):ExplosionView {
        const explosion:ExplosionView = explosions[id];
        if (!explosion) throw new Error("can't find explosion " + id);
        return explosion;
    }

    public function addExplosion(id:int, pos:Point):void {
        const explosion:ExplosionView = new ExplosionView();
        explosion.x = pos.x;
        explosion.y = pos.y;
        if (explosion[id]) throw new Error("explosion " + id + " already exists");
        explosions[id] = explosion;
        addChild(explosion);
    }

    public function removeExplosion(id:int):void {
        removeChild(byId(id));
        delete explosions[id];
    }
}
}

package ru.rknrl.castles.view.game.area.explosions {
import flash.display.Sprite;
import flash.utils.Dictionary;

import ru.rknrl.castles.model.points.Point;

public class ExplosionsView extends Sprite {
    private const explosions:Dictionary = new Dictionary();

    private function byId(id:int):ExplosionMC {
        const explosion:ExplosionMC = explosions[id];
        if (!explosion) throw new Error("can't find explosion " + id);
        return explosion;
    }

    public function addExplosion(id:int, pos:Point):void {
        const explosion:ExplosionMC = new ExplosionMC();
        explosion.x = pos.x;
        explosion.y = pos.y;
        if (explosions[id]) throw new Error("explosion " + id + " already exists");
        explosions[id] = explosion;
        addChild(explosion);
    }

    public function removeExplosion(id:int):void {
        removeChild(byId(id));
        delete explosions[id];
    }
}
}

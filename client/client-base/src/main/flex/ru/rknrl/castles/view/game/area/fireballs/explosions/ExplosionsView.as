package ru.rknrl.castles.view.game.area.fireballs.explosions {
import flash.display.Sprite;
import flash.utils.Dictionary;

import ru.rknrl.castles.utils.points.Point;

public class ExplosionsView extends Sprite {
    private const explosions:Dictionary = new Dictionary();

    public function addExplosion(id:int, pos:Point):void {
        const explosion:ExplosionView = new ExplosionView();
        explosion.x = pos.x;
        explosion.y = pos.y;
        addChild(explosion);
        explosions[id] = explosion;
    }

    public function removeExplosion(id:int):void {
        const explosion:ExplosionView = explosions[id];
        removeChild(explosion);
        delete explosions[id];
    }
}
}

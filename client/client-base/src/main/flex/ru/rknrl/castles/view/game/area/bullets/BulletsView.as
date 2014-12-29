package ru.rknrl.castles.view.game.area.bullets {
import flash.display.Sprite;
import flash.utils.Dictionary;

import ru.rknrl.castles.utils.points.Point;

public class BulletsView extends Sprite {
    private const bullets:Dictionary = new Dictionary();

    public function addBullet(id:int, pos:Point):void {
        const bullet:BulletView = new BulletView(pos);
        addChild(bullet);
        bullets[id] = bullet;
    }

    public function setBulletPos(id:int, pos:Point):void {
        const bullet:BulletView = bullets[id];
        bullet.pos = pos;
    }

    public function removeBullet(id:int):void {
        const bullet:BulletView = bullets[id];
        removeChild(bullet);
        delete bullets[id];
    }
}
}

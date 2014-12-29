package ru.rknrl.castles.view.game.area.bullets {
import flash.display.Sprite;
import flash.utils.Dictionary;

import ru.rknrl.castles.utils.points.Point;

public class BulletsView extends Sprite {
    private const bullets:Dictionary = new Dictionary();

    private function byId(id:int):BulletView {
        const bullet:BulletView = bullets[id];
        if (!bullet) throw new Error("can't find bullet " + id);
        return bullet;
    }

    public function addBullet(id:int, pos:Point):void {
        const bullet:BulletView = new BulletView(pos);
        if (bullets[id]) throw new Error("bullet " + id + " already exists");
        bullets[id] = bullet;
        addChild(bullet);
    }

    public function setBulletPos(id:int, pos:Point):void {
        byId(id).pos = pos;
    }

    public function removeBullet(id:int):void {
        removeChild(byId(id));
        delete bullets[id];
    }
}
}

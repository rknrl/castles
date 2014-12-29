package ru.rknrl.castles.controller.game {
import ru.rknrl.castles.model.game.Bullet;
import ru.rknrl.castles.utils.points.Point;
import ru.rknrl.castles.view.game.area.bullets.BulletsView;

public class Bullets {
    private var view:BulletsView;

    public function Bullets(view:BulletsView) {
        this.view = view;
    }

    private const bullets:Vector.<Bullet> = new <Bullet>[];
    private var bulletIterator:int;

    public function add(time:int, startPos:Point, endPos:Point, duration:int):void {
        const bullet:Bullet = new Bullet(bulletIterator++, startPos, endPos, time, duration);
        bullets.push(bullet);
        view.addBullet(bullet.id, bullet.pos(time));
    }

    public function update(time:int):void {
        const bulletsToRemove:Vector.<Bullet> = new <Bullet>[];

        for each(var bullet:Bullet in bullets) {
            view.setBulletPos(bullet.id, bullet.pos(time));
            if (bullet.needRemove(time)) bulletsToRemove.push(bullet);
        }

        for each(bullet in bulletsToRemove) {
            const index:int = bullets.indexOf(bullet);
            bullets.splice(index, 1);
            view.removeBullet(bullet.id);
        }
    }

}
}

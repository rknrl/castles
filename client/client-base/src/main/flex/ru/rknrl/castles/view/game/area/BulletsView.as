package ru.rknrl.castles.view.game.area {
import ru.rknrl.castles.model.points.Point;

public class BulletsView extends MovableView {
    public function BulletsView():void {
        super("bullet");
    }

    public function addBullet(id:int, pos:Point):void {
        add(id, pos, new BulletMC());
    }
}
}

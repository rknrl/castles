package ru.rknrl.castles.view.game.area {
import ru.rknrl.castles.model.points.Point;

public class FireballsView extends MovableView {
    public function FireballsView():void {
        super("fireball");
    }

    public function addFireball(id:int, pos:Point):void {
        add(id, pos, new FireballMC());
    }
}
}

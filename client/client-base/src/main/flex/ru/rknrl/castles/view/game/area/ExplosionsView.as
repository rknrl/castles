package ru.rknrl.castles.view.game.area {
import ru.rknrl.castles.model.points.Point;

public class ExplosionsView extends PeriodicView {
    public function ExplosionsView() {
        super("explosion");
    }

    public function addExplosion(id:int, pos:Point):void {
        add(id, pos, new ExplosionMC());
    }
}
}

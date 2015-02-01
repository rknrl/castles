package ru.rknrl.castles.view.game.area.volcanoes {
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.view.game.area.PeriodicView;

public class VolcanoesView extends PeriodicView {
    public function VolcanoesView() {
        super("volcano");
    }

    public function addVolcano(id:int, pos:Point, radius:Number):void {
        add(id, pos, new VolcanoView(radius));
    }

    public function setVolcanoRadius(id:int, radius:Number):void {
        VolcanoView(byId(id)).radius = radius;
    }
}
}

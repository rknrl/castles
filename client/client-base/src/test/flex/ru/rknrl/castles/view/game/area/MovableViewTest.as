package ru.rknrl.castles.view.game.area {
import org.flexunit.asserts.assertEquals;

import ru.rknrl.castles.model.points.Point;

public class MovableViewTest {
    [Test(description="Измение координат работает")]
    public function t4():void {
        const movableView:FireballsView = new FireballsView();
        movableView.addFireball(0, new Point(100, 77));
        movableView.addFireball(1, new Point(5, 5));

        movableView.setPos(0, new Point(1, 1));
        assertEquals(movableView.getChildAt(0).x, 1);
        assertEquals(movableView.getChildAt(0).y, 1);
        assertEquals(movableView.getChildAt(1).x, 5);
        assertEquals(movableView.getChildAt(1).y, 5);

        movableView.setPos(1, new Point(1024, 768));
        assertEquals(movableView.getChildAt(0).x, 1);
        assertEquals(movableView.getChildAt(0).y, 1);
        assertEquals(movableView.getChildAt(1).x, 1024);
        assertEquals(movableView.getChildAt(1).y, 768);
    }
}
}

package ru.rknrl.castles.view.game.area {
import org.flexunit.asserts.assertEquals;

import ru.rknrl.castles.model.points.Point;

public class PeriodicViewTest {
    [Test(description="Добавление-удаление два раза подряд происходит нормально")]
    public function t1():void {
        const periodicView:FireballsView = new FireballsView();
        periodicView.addFireball(1, new Point(0, 0));
        periodicView.remove(1);
        periodicView.addFireball(1, new Point(0, 0));
        periodicView.remove(1);

        assertEquals(periodicView.numChildren, 0)
    }

    [Test(expects="Error", description="Добавление два раза подряд бросает эксепшн")]
    public function t2():void {
        const periodicView:FireballsView = new FireballsView();
        periodicView.addFireball(2, new Point(0, 0));
        periodicView.addFireball(2, new Point(0, 0));
    }

    [Test(expects="Error", description="Удаление два раза подряд бросает эксепшн")]
    public function t3():void {
        const periodicView:FireballsView = new FireballsView();
        periodicView.addFireball(0, new Point(0, 0));
        periodicView.remove(0);
        periodicView.remove(0);
    }

    [Test(description="После добавления объект имеет верное положение")]
    public function t4():void {
        const periodicView:FireballsView = new FireballsView();
        periodicView.addFireball(0, new Point(100, 77));

        assertEquals(periodicView.getChildAt(0).x, 100);
        assertEquals(periodicView.getChildAt(0).y, 77);
    }
}
}

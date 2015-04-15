//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.controller.game {
import flash.display.Sprite;

import org.flexunit.asserts.assertEquals;

import ru.rknrl.castles.view.game.area.VolcanoView;
import ru.rknrl.core.Static;
import ru.rknrl.core.points.Point;

public class VolcanoesControllerTest {
    [Test("getRadius")]
    public function t1():void {
        const volcano:Static = new Static(new Point(1, 1), 1, 3);
        assertEquals(20, VolcanoesController.getRadius(1, volcano));
        assertEquals(30, VolcanoesController.getRadius(2, volcano));
        assertEquals(40, VolcanoesController.getRadius(3, volcano));
        assertEquals(40, VolcanoesController.getRadius(12, volcano));
    }

    [Test("")]
    public function t2():void {
        const layer:Sprite = new Sprite();
        const controller:VolcanoesController = new VolcanoesController(layer);

        const volcano:Static = new Static(new Point(1, 2), 1, 3);
        const volcanoView:VolcanoView = new VolcanoView();
        controller.add(1, volcano, volcanoView);

        assertEquals(20, volcanoView.radius);

        controller.update(2);
        assertEquals(30, volcanoView.radius);

        controller.update(3);
        assertEquals(40, volcanoView.radius);

    }
}
}

//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.controller.game {
import flash.display.DisplayObject;

import org.flexunit.asserts.assertEquals;
import org.flexunit.asserts.assertFalse;

import ru.rknrl.castles.view.game.area.arrows.ArrowsView;
import ru.rknrl.core.points.Point;

public class ArrowsTest {
    [Test("add arrow")]
    public function t0():void {
        const view:ArrowsView = new ArrowsView();
        const arrows:Arrows = new Arrows(view);
        arrows.addArrow(0, new Point(20, 30));
        assertEquals(1, view.numChildren);
        const arrowView:DisplayObject = view.getChildAt(0);
        assertEquals(20, arrowView.x);
        assertEquals(30, arrowView.y);
    }

    [Test("remove arrow")]
    public function t1():void {
        const view:ArrowsView = new ArrowsView();
        const arrows:Arrows = new Arrows(view);
        arrows.addArrow(0, new Point(20, 30));
        arrows.removeArrow(0);
        assertFalse(arrows.hasArrow(0));
        assertEquals(0, view.numChildren);
    }
}
}

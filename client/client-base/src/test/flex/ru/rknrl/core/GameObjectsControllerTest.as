//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.core {
import flash.display.Sprite;

import org.flexunit.asserts.assertEquals;

import ru.rknrl.core.points.Point;
import ru.rknrl.core.points.Points;

public class GameObjectsControllerTest {
    [Test("add")]
    public function t1():void {
        const layer:Sprite = new Sprite();
        const controller:GameObjectsController = new GameObjectsController(layer);

        const points:Points = Points.two(new Point(1, 1), new Point(2, 2));
        const movable:Movable = new Movable(points, 1, 10);
        const movableView:Sprite = new Sprite();
        controller.add(2, movable, movableView);

        assertEquals(1, layer.numChildren);
        assertEquals(movableView, layer.getChildAt(0));
        const expectedPos:Point = movable.pos(2);
        assertEquals(expectedPos.x, movableView.x);
        assertEquals(expectedPos.y, movableView.y);
    }

    [Test("add twice", expects="Error")]
    public function t2():void {
        const layer:Sprite = new Sprite();
        const controller:GameObjectsController = new GameObjectsController(layer);

        const points:Points = Points.two(new Point(1, 1), new Point(2, 2));
        const movable:Movable = new Movable(points, 1, 10);
        const movableView:Sprite = new Sprite();
        controller.add(2, movable, movableView);
        controller.add(2, movable, movableView);
    }

    [Test("remove")]
    public function t3():void {
        const layer:Sprite = new Sprite();
        const controller:GameObjectsController = new GameObjectsController(layer);

        const points:Points = Points.two(new Point(1, 1), new Point(2, 2));
        const movable:Movable = new Movable(points, 1, 10);
        const movableView:Sprite = new Sprite();
        controller.add(2, movable, movableView);
        controller.remove(3, movable);

        assertEquals(0, layer.numChildren);
    }

    [Test("remove empty DONT throw error")]
    public function t4():void {
        const layer:Sprite = new Sprite();
        const controller:GameObjectsController = new GameObjectsController(layer);

        const points:Points = Points.two(new Point(1, 1), new Point(2, 2));
        const movable:Movable = new Movable(points, 1, 10);
        controller.remove(3, movable);
    }

    [Test("update pos")]
    public function t5():void {
        const layer:Sprite = new Sprite();
        const controller:GameObjectsController = new GameObjectsController(layer);

        const points:Points = Points.two(new Point(1, 1), new Point(2, 2));
        const movable:Movable = new Movable(points, 1, 10);
        const movableView:Sprite = new Sprite();
        controller.add(2, movable, movableView);
        controller.update(4);

        assertEquals(1, layer.numChildren);
        assertEquals(movableView, layer.getChildAt(0));

        const expectedPos:Point = movable.pos(4);
        assertEquals(expectedPos.x, movableView.x);
        assertEquals(expectedPos.y, movableView.y);
    }

    [Test("cleanup")]
    public function t6():void {
        const layer:Sprite = new Sprite();
        const controller:GameObjectsController = new GameObjectsController(layer);

        const points:Points = Points.two(new Point(1, 1), new Point(2, 2));
        const movable:Movable = new Movable(points, 1, 10);
        const movableView:Sprite = new Sprite();
        controller.add(2, movable, movableView);
        controller.update(11);

        assertEquals(0, layer.numChildren);
    }
}
}
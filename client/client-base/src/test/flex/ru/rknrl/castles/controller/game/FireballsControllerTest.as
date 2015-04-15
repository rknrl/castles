//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.controller.game {
import flash.display.DisplayObject;
import flash.display.Sprite;

import org.flexunit.asserts.assertEquals;

import ru.rknrl.core.Movable;
import ru.rknrl.core.points.Point;
import ru.rknrl.core.points.Points;

public class FireballsControllerTest {
    [Test("add/remove explosion")]
    public function t1():void {
        const fireballsLayer:Sprite = new Sprite();
        const explosionsLayer:Sprite = new Sprite();
        const spriteFactory:SpriteFactory = new SpriteFactory();
        const controller:FireballsController = new FireballsController(fireballsLayer, explosionsLayer, spriteFactory);

        const points:Points = Points.two(new Point(1, 1), new Point(2, 3));
        const fireball:Movable = new Movable(points, 1, 10);
        const fireballView:Sprite = new Sprite();
        controller.add(2, fireball, fireballView);

        assertEquals(0, explosionsLayer.numChildren);

        controller.update(11);

        assertEquals(1, explosionsLayer.numChildren);
        const explosion:DisplayObject = explosionsLayer.getChildAt(0);
        assertEquals(2, explosion.x);
        assertEquals(3, explosion.y);

        controller.update(11 + FireballsController.explosionDuration);
        assertEquals(0, explosionsLayer.numChildren);
    }
}
}

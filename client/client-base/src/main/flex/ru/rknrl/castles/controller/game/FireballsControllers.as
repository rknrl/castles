//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.controller.game {
import flash.display.Sprite;

import ru.rknrl.core.GameObject;
import ru.rknrl.core.GameObjectViewFactory;
import ru.rknrl.core.GameObjectsController;
import ru.rknrl.core.Static;

public class FireballsControllers extends GameObjectsController {
    private static const explosionDuration:int = 500;

    private var explosions:GameObjectsController;
    private var explosionsFactory:GameObjectViewFactory;

    public function FireballsControllers(fireballsLayer:Sprite,
                                         explosionsLayer:Sprite, explosionsFactory:GameObjectViewFactory) {
        this.explosionsFactory = explosionsFactory;
        explosions = new GameObjectsController(explosionsLayer);
        super(fireballsLayer);
    }

    override public function remove(time:int, object:GameObject):void {
        super.remove(time, object);
        const explosion:Static = new Static(object.pos(time), time, explosionDuration);
        explosions.add(time, explosion, explosionsFactory.create(time));
    }

    override public function update(time:int):void {
        super.update(time);
        explosions.update(time);
    }
}
}

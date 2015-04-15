//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game.area.factories {
import flash.display.DisplayObject;
import flash.utils.getTimer;

import ru.rknrl.castles.view.utils.dust.Dust;
import ru.rknrl.core.GameObjectViewFactory;

public class ExplosionViewFactory implements GameObjectViewFactory {
    public function create(time: int):DisplayObject {
        return new Dust(time);
    }
}
}

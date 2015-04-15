//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game.area.factories {
import flash.display.DisplayObject;

import ru.rknrl.castles.view.game.area.VolcanoView;
import ru.rknrl.core.GameObjectViewFactory;

public class VolcanoViewFactory implements GameObjectViewFactory {
    public function create(time:int):DisplayObject {
        return new VolcanoView();
    }
}
}

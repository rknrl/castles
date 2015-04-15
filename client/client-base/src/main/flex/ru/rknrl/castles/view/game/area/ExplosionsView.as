//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game.area {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.events.Event;

import ru.rknrl.castles.model.events.GameViewEvents;

public class ExplosionsView extends Sprite {
    override public function addChild(dust:DisplayObject):DisplayObject {
        dispatchEvent(new Event(GameViewEvents.SHAKE, true));
        return super.addChild(dust);
    }
}
}

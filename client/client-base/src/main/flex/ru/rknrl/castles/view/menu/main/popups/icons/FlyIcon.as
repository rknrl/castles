//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.menu.main.popups.icons {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.events.Event;

import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.utils.Fly;
import ru.rknrl.castles.view.utils.Shadow;

public class FlyIcon extends Sprite {
    private var fly:Fly;

    public function FlyIcon(icon:DisplayObject) {
        const shadow:Shadow = new Shadow();
        shadow.y = Layout.popupFlyIconShadowY;
        addChild(shadow);

        icon.y = -Layout.shadowDistance + Layout.popupFlyIconShadowY;
        addChild(icon);

        fly = new Fly(icon, shadow);

        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    private function onEnterFrame(event:Event):void {
        fly.onEnterFrame();
    }
}
}

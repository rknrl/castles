package ru.rknrl.castles.view.menu.main.popups.icons {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.events.Event;

import ru.rknrl.castles.view.utils.Fly;
import ru.rknrl.castles.view.utils.Shadow;
import ru.rknrl.castles.view.layout.Layout;

public class FlyIcon extends Sprite {
    private var fly:Fly;

    public function FlyIcon(icon:DisplayObject) {
        const shadow:Shadow = new Shadow();
        shadow.y = Layout.popupShadowY;
        addChild(shadow);

        icon.y = -Layout.shadowDistance + Layout.popupShadowY;
        addChild(icon);

        fly = new Fly(icon, shadow);

        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    private function onEnterFrame(event:Event):void {
        fly.onEnterFrame();
    }
}
}

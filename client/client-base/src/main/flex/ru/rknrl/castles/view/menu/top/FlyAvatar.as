package ru.rknrl.castles.view.menu.top {
import flash.display.Sprite;
import flash.events.Event;

import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.utils.Fly;
import ru.rknrl.castles.view.utils.LoadImageManager;
import ru.rknrl.castles.view.utils.Shadow;

public class FlyAvatar extends Sprite {
    private var avatar:Avatar;
    private var fly:Fly;

    public function FlyAvatar(photoUrl:String, bitmapDataScale:Number, loadImageManager:LoadImageManager) {
        const shadow:Shadow = new Shadow();
        shadow.y = Layout.shadowDistance;
        addChild(shadow);

        addChild(avatar = new Avatar(photoUrl, Layout.itemSize, bitmapDataScale, loadImageManager));

        fly = new Fly(avatar, shadow);

        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    public function set bitmapDataScale(value:Number):void {
        avatar.bitmapDataScale = value;
    }

    private function onEnterFrame(event:Event):void {
        fly.onEnterFrame();
    }
}
}

//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view {
import flash.display.Bitmap;
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.events.Event;

import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.utils.AnimatedShadow;
import ru.rknrl.castles.view.utils.Fly;

public class SocialIcon extends Sprite {
    private static const mouseHolderW:Number = Layout.itemSize + Layout.itemGap;
    private static const mouseHolderH:Number = 96;

    private var fly:Fly;

    private var _socialName:String;

    public function get socialName():String {
        return _socialName;
    }

    public function SocialIcon(socialName:String, icon:DisplayObject) {
        _socialName = socialName;
        mouseChildren = false;

        const mouseHolder:Bitmap = new Bitmap(Colors.transparent);
        mouseHolder.width = mouseHolderW;
        mouseHolder.height = mouseHolderH;
        mouseHolder.x = -mouseHolderW / 2;
        mouseHolder.y = -40;
        addChild(mouseHolder);

        addChild(icon);

        const shadow:AnimatedShadow = new AnimatedShadow();
        shadow.y = Layout.shadowDistance;
        addChild(shadow);

        fly = new Fly(icon, shadow);
        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    private function onEnterFrame(event:Event):void {
        fly.onEnterFrame();
    }
}
}

package ru.rknrl.castles.menu.screens.main.popup {
import flash.display.Bitmap;
import flash.display.Sprite;

import ru.rknrl.castles.utils.Utils;
import ru.rknrl.castles.utils.layout.Layout;

public class PopupScreen extends Sprite {
    private var bitmap:Bitmap;

    public function PopupScreen(layout:Layout) {
        addChild(bitmap = new Bitmap(Utils.popupScreen));
        updateLayout(layout);
    }

    public function updateLayout(layout:Layout):void {
        bitmap.width = layout.stageWidth;
        bitmap.height = layout.stageHeight;
    }

    public function set transition(value:Number):void {
        alpha = value;
    }
}
}

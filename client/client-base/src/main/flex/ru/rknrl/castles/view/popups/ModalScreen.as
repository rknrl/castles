package ru.rknrl.castles.view.popups {
import flash.display.Bitmap;
import flash.display.BitmapData;
import flash.display.Sprite;
import flash.events.Event;
import flash.events.MouseEvent;

import ru.rknrl.castles.view.layout.Layout;

public class ModalScreen extends Sprite {
    private var bitmap:Bitmap;

    public function ModalScreen(layout:Layout) {
        addChild(bitmap = new Bitmap(new BitmapData(1, 1, true, 0x44000000)));
        this.layout = layout;
        addEventListener(MouseEvent.MOUSE_DOWN, onClick);
    }

    public function set layout(value:Layout):void {
        bitmap.width = value.screenWidth;
        bitmap.height = value.screenHeight;
    }

    public function set transition(value:Number):void {
        alpha = value;
    }

    private function onClick(event:MouseEvent):void {
        dispatchEvent(new Event(PopupEvent.CLOSE, true));
    }
}
}

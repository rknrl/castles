package ru.rknrl.castles.view.popups {
import flash.display.Bitmap;
import flash.display.Sprite;
import flash.events.Event;
import flash.events.MouseEvent;

import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.layout.Layout;

public class ModalScreen extends Sprite {
    private var bitmap:Bitmap;

    public function ModalScreen(layout:Layout) {
        addChild(bitmap = new Bitmap(Colors.modalBitmapData));
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

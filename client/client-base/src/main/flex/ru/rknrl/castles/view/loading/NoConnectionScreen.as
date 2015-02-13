//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.loading {
import flash.display.Bitmap;
import flash.events.Event;
import flash.events.MouseEvent;

import ru.rknrl.castles.model.events.ViewEvents;
import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.layout.Layout;

public class NoConnectionScreen extends LoadingScreen {
    private var mouseHolder:Bitmap;

    public function NoConnectionScreen(text:String, layout:Layout) {
        addChild(mouseHolder = new Bitmap(Colors.transparent));
        super(text, layout);
        addEventListener(MouseEvent.MOUSE_DOWN, onClick);
    }

    override public function set layout(value:Layout):void {
        super.layout = value;
        mouseHolder.width = value.screenWidth;
        mouseHolder.height = value.screenHeight;
    }

    private function onClick(event:MouseEvent):void {
        dispatchEvent(new Event(ViewEvents.TRY_CONNECT, true));
    }
}
}

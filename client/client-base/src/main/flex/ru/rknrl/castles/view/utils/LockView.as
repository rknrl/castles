//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.utils {
import flash.display.Sprite;
import flash.events.Event;
import flash.utils.getTimer;

public class LockView extends Sprite {
    private var bar:Sprite;

    public function LockView() {
        addChild(bar = new LockMC());
        addEventListener(Event.ENTER_FRAME, onEnterFrame);
        visible = false;
    }

    private function onEnterFrame(event:Event):void {
        const speed:Number = 5;
        bar.rotation = (getTimer() / speed) % 360;
    }
}
}

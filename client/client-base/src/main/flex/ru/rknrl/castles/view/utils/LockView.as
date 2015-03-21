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
    private static const visibleDelay:int = 1000;
    private static const rotateSpeed:Number = 5;

    private var bar:Sprite;

    public function LockView() {
        addChild(bar = new LockMC());
        addEventListener(Event.ENTER_FRAME, onEnterFrame);
        visible = false;
    }

    private var setVisibleTime:Number = NaN;

    override public function set visible(value:Boolean):void {
        if (value) {
            setVisibleTime = getTimer();
        } else {
            super.visible = false;
            setVisibleTime = NaN;
        }
    }

    private function onEnterFrame(event:Event):void {
        const time:int = getTimer();
        bar.rotation = (time / rotateSpeed) % 360;

        if (setVisibleTime && time - setVisibleTime > visibleDelay) {
            super.visible = true;
            setVisibleTime = NaN;
        }
    }
}
}

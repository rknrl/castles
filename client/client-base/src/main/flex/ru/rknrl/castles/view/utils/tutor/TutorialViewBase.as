//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.utils.tutor {
import flash.display.Sprite;
import flash.events.Event;

import ru.rknrl.castles.view.layout.Layout;

public class TutorialViewBase extends Sprite {
    public function TutorialViewBase(layout:Layout) {
        visible = false;
        mouseEnabled = mouseChildren = false;

        _layout = layout;
    }

    private var _layout:Layout;

    public function get layout():Layout {
        return _layout;
    }

    public function set layout(value:Layout):void {
        _layout = value;
    }

    protected final function show():void {
        visible = true;
    }

    public function hide(event:Event = null):void {
        visible = false;
    }

    public final function get playing():Boolean {
        return visible;
    }
}
}

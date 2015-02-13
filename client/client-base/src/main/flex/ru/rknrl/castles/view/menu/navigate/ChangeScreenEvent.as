//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.menu.navigate {
import flash.events.Event;

public class ChangeScreenEvent extends Event {
    public static const CHANGE_SCREEN:String = "changeScreen";

    private var _screen:Screen;

    public function get screen():Screen {
        return _screen;
    }

    public function ChangeScreenEvent(screen:Screen) {
        super(CHANGE_SCREEN, true);
        _screen = screen;
    }
}
}

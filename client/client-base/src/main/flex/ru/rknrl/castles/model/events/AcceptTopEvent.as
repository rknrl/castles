//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.events {
import flash.events.Event;

import protos.Top;

import ru.rknrl.castles.model.menu.top.TopUtils;

public class AcceptTopEvent extends Event {
    public static const ACCEPT_TOP:String = "acceptTop";

    private var _top:Top;

    public function get top():Top {
        return _top;
    }

    public function AcceptTopEvent(top:Top) {
        super(ACCEPT_TOP, true);
        _top = top;
    }
}
}

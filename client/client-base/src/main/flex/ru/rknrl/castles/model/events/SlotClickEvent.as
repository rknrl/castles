//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.events {
import flash.events.Event;

import protos.SlotId;

public class SlotClickEvent extends Event {
    public static const SLOT_CLICK:String = "slotClick";

    private var _slotId:SlotId;

    public function get slotId():SlotId {
        return _slotId;
    }

    public function SlotClickEvent(slotId:SlotId) {
        _slotId = slotId;
        super(SLOT_CLICK, true);
    }
}
}

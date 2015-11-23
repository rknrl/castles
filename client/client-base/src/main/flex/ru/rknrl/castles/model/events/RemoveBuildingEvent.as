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

public class RemoveBuildingEvent extends Event {
    public static const REMOVE_BUILDING:String = "removeBuilding";

    private var _slotId:SlotId;

    public function get slotId():SlotId {
        return _slotId;
    }

    public function RemoveBuildingEvent(slotId:SlotId) {
        _slotId = slotId;
        super(REMOVE_BUILDING, true);
    }
}
}
